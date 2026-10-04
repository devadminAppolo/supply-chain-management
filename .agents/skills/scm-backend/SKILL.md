---
name: scm-backend
description: Development guide, architecture patterns, coding conventions, database guidelines, and implementation checklist for the Supply Chain Management (SCM) platform. Combines clean architecture conventions, MapStruct, JPA specifications, transactional workflows, Flyway migration protection, and Yard360 best practices.
---

# Supply Chain Management (SCM) Backend Development Guide (`skill.md`)

This document defines the architectural patterns, clean architecture layering, backend coding conventions, database guidelines, reusability practices, and step-by-step implementation rules for the **Supply Chain Management (SCM)** backend codebase (`com.supplychain`).

---

## 1. Project Overview

### 1.1 Purpose & Core Modules
The **Supply Chain Management (SCM)** platform is an enterprise-grade logistics and warehouse management system built around Domain-Driven Design (DDD) principles. It manages the complete lifecycle of goods across the supply chain:

- **Procurement**:
  - **Purchase Orders (PO)**: PO authoring, line items, line status tracking, lifecycle management, and XML EDI import parsing.
  - **Advance Shipping Notices (ASN)**: Inbound shipping notices linked to PO line allocations, status progression, validation, and cancel workflows.
- **Warehouse Management System (WMS)**:
  - **Receiving & Putaway**: Inbound receipt confirmation, inspection, stage locations, and putaway task execution.
  - **Pallet & LPN Management**: Pallet master registry, License Plate Numbers (LPN), barcodes, tare weight, dimensions, and lifecycle statuses.
  - **Location & Bin Transfers**: Multi-zone hierarchy (Aisle, Rack, Shelf, Bin), location capacities, and bin-to-bin stock movements.
  - **Inventory & Adjustments**: Real-time on-hand, allocated, and available quantity tracking, adjustments with audit reasons, and transaction history.
  - **Staff & Operations**: Warehouse personnel assignments and operational dashboards.
- **Outbound & Fulfillment**:
  - **Outbound Orders**: Customer sales order fulfillment and wave planning.
  - **Picking & Packing**: Item picking routes, packing stations, and cartonization.
  - **Transport Orders**: Carrier dispatching, load tendering, and vehicle transport orders.
- **Transportation Management (TMS)**:
  - **Shipments**: Inbound and outbound shipment planning, driver details, carrier tracking, and freight status.
- **Master Data Catalog**:
  - **Items, Suppliers, Customers, Carriers, and Drivers**: Centralized enterprise master data.

### 1.2 Technology Stack
| Layer / Component | Technology / Version |
| :--- | :--- |
| **Java Version** | Java 21 (Toolchain configured) |
| **Framework** | Spring Boot `3.4.1` |
| **Dependency Management** | Spring Dependency Management `1.1.7` |
| **Database** | PostgreSQL (`org.postgresql:postgresql`) |
| **Database Schema** | `scm` (Hibernate default schema) |
| **Database Migration** | Flyway (`org.flywaydb:flyway-database-postgresql`) |
| **ORM / Persistence** | Spring Data JPA, Hibernate `6.x` (`ddl-auto=validate`) |
| **Object Mapping** | MapStruct `1.6.3` (`mapstruct-processor`) |
| **Boilerplate Reduction** | Project Lombok (`@Getter`, `@Setter`, `@SuperBuilder`, `@RequiredArgsConstructor`) |
| **Validation** | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| **API Envelope & Formats** | Unified `ApiResponse<T>` and generic `PageResponse<T>` |
| **Testing & Mocking** | JUnit 5 (`org.junit.jupiter`), Spring Boot Starter Test, Datafaker `2.4.2` |

### 1.3 High-Level Architecture
The project follows a **Domain-Driven Modular & Clean Architecture** structure:

```
[ Presentation Layer ]  -->  Controllers (REST endpoints, @Valid, ApiResponse envelope)
        │
[ Application Layer ]   -->  Services (Interfaces + Impls, @Transactional, business rules, DTOs)
        │
[ Domain Layer ]        -->  Entities (extend BaseEntity, @SuperBuilder), Enums, Business rules
        │
[ Infrastructure Layer ] -->  Repositories (Spring Data JPA + JpaSpecificationExecutor), Mappers (MapStruct)
```

---

## 2. Folder and Package Structure

```
w:\SCM\supply-chain-management\
├── build.gradle
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── supplychain
    │   │           ├── SupplyChainManagementApplication.java
    │   │           ├── common/                               # Shared cross-cutting components
    │   │           │   ├── application/dto/                  # ApiResponse<T>, BaseDto, PageResponse<T>, DropdownOptionDto
    │   │           │   ├── domain/                           # BaseEntity (@MappedSuperclass), common Enums
    │   │           │   ├── exception/                        # GlobalExceptionHandler (@RestControllerAdvice)
    │   │           │   └── infrastructure/config/            # CorsConfig, JpaAuditingConfig
    │   │           ├── procurement/                          # Procurement module
    │   │           │   ├── asn/                              # Advance Shipping Notice feature
    │   │           │   │   ├── application/dto/
    │   │           │   │   ├── application/service/
    │   │           │   │   ├── domain/enums/
    │   │           │   │   ├── domain/model/
    │   │           │   │   ├── infrastructure/mapper/
    │   │           │   │   ├── infrastructure/repository/
    │   │           │   │   └── presentation/controller/
    │   │           │   └── purchaseorder/                    # Purchase Order feature
    │   │           ├── wms/                                  # Warehouse Management module
    │   │           │   ├── bintransfer/
    │   │           │   ├── dashboard/
    │   │           │   ├── inventory/
    │   │           │   ├── inventoryadjustment/
    │   │           │   ├── inventorytransaction/
    │   │           │   ├── location/
    │   │           │   ├── pallet/
    │   │           │   ├── putaway/
    │   │           │   ├── receiving/
    │   │           │   └── staff/
    │   │           ├── outbound/                             # Outbound & Fulfillment module
    │   │           │   ├── outboundorder/
    │   │           │   ├── packing/
    │   │           │   ├── picking/
    │   │           │   └── transportorder/
    │   │           ├── tms/                                  # Transportation Management module
    │   │           │   └── shipment/
    │   │           └── catalog/                              # Master data catalog
    │   │               ├── carrier/
    │   │               ├── customer/
    │   │               ├── driver/
    │   │               ├── item/
    │   │               └── supplier/
    │   └── resources
    │       ├── application.properties                        # Server, Datasource, JPA, Flyway config
    │       └── db
    │           └── migration
    │               ├── V1__Create_SCM_Schema.sql
    │               ├── ...
    │               └── V21__Create_Transport_Order_Schema.sql
    └── test
        └── java
            └── com
                └── supplychain
                    └── SupplyChainManagementApplicationTests.java
```

### 2.1 Package Placement Rules
When implementing or extending any feature:
- **Never create ad-hoc root folders**. Place features inside their functional parent domain (`procurement`, `wms`, `outbound`, `tms`, `catalog`, or `common`).
- Each feature package MUST follow the 4-layer structure:
  1. `application/dto/`: Request DTOs (`*RequestDto.java`) and Response DTOs (`*ResponseDto.java`).
  2. `application/service/`: Service interface (`*Service.java`) and Implementation (`*ServiceImpl.java`).
  3. `domain/model/`: JPA Entities extending `BaseEntity`.
  4. `domain/enums/`: Domain-specific enumeration types.
  5. `infrastructure/mapper/`: MapStruct interface mapper (`*Mapper.java`).
  6. `infrastructure/repository/`: Spring Data JPA repository (`*Repository.java`).
  7. `presentation/controller/`: Spring REST controller (`*Controller.java`).

---

## 3. Backend Coding Standards

### 3.1 Controller Conventions
1. **Annotations**: Use `@RestController`, `@RequestMapping("/api/<resource-plural>")`, `@RequiredArgsConstructor`.
2. **Constructor Injection**: Always use Lombok `@RequiredArgsConstructor` with `private final` service fields.
3. **Response Wrapping**: All controller methods return `ResponseEntity<ApiResponse<T>>` or `ResponseEntity<Void>`.
4. **Validation**: Place `@Valid` on `@RequestBody` parameters to activate Jakarta Bean Validation.
5. **Pagination**: Use Spring Data `@PageableDefault` with standard page size and sort directions.
6. **Standard HTTP Methods**:
   - `POST /api/<resources>` $\to$ `create(@Valid @RequestBody CreateRequestDto)` $\to$ returns `HttpStatus.CREATED` with message.
   - `PUT /api/<resources>/{id}` $\to$ `update(@PathVariable Long id, @Valid @RequestBody UpdateRequestDto)` $\to$ returns `HttpStatus.OK`.
   - `PATCH /api/<resources>/{id}/<action>` $\to$ State/status transition (e.g., cancel, approve, complete).
   - `GET /api/<resources>/{id}` $\to$ `findById(@PathVariable Long id)`.
   - `GET /api/<resources>` $\to$ `findAll(search criteria, @PageableDefault Pageable pageable)`.
   - `DELETE /api/<resources>/{id}` $\to$ `deleteById(@PathVariable Long id)` $\to$ returns `ResponseEntity.noContent().build()`.
   - `DELETE /api/<resources>/batch` $\to$ `deleteAllByIds(@RequestBody List<Long> ids)` $\to$ returns `ResponseEntity.noContent().build()`.

#### Example Controller Pattern:
```java
package com.supplychain.wms.pallet.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.pallet.application.dto.CreatePalletRequestDto;
import com.supplychain.wms.pallet.application.dto.PalletResponseDto;
import com.supplychain.wms.pallet.application.dto.UpdatePalletRequestDto;
import com.supplychain.wms.pallet.application.service.PalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pallets")
@RequiredArgsConstructor
public class PalletController {

    private final PalletService palletService;

    @PostMapping
    public ResponseEntity<ApiResponse<PalletResponseDto>> create(@Valid @RequestBody CreatePalletRequestDto request) {
        PalletResponseDto created = palletService.createPallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pallet created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePalletRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Pallet updated successfully", palletService.updatePallet(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PalletResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findAll(search, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        palletService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        palletService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 3.2 Service Implementation Patterns
1. **Interface & Implementation Split**: Always create `FooService` interface and `FooServiceImpl` class.
2. **Annotations**: Annotate implementation with `@Slf4j`, `@Service`, `@RequiredArgsConstructor`.
3. **Transactions**:
   - Class-level or method-level `@Transactional` for state mutations.
   - `@Transactional(readOnly = true)` for query methods.
4. **Exception Throwing**:
   - Throw `ResponseStatusException(HttpStatus.NOT_FOUND, "...")` when entity is not found.
   - Throw `ResponseStatusException(HttpStatus.CONFLICT, "...")` on uniqueness violations or illegal state transitions.
   - Throw `ResponseStatusException(HttpStatus.BAD_REQUEST, "...")` on domain validation failures.
5. **Soft Delete Handling**: When checking existence or retrieving by ID, always enforce `deleted = false` (`findByIdAndDeletedFalse`, `existsBy...AndDeletedFalse`).
6. **Concurrency Controls (Yard360 best practice)**:
   - For high-contention resources (e.g. inventory allocations, PO line deduction), use pessimistic locking (`findAllByIdInForUpdate` or `@Lock(LockModeType.PESSIMISTIC_WRITE)`).
   - For entity updates, leverage `@Version` optimistic locking in entities.

#### Example Service Implementation Pattern:
```java
package com.supplychain.wms.pallet.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.pallet.application.dto.CreatePalletRequestDto;
import com.supplychain.wms.pallet.application.dto.PalletResponseDto;
import com.supplychain.wms.pallet.application.dto.UpdatePalletRequestDto;
import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.model.Pallet;
import com.supplychain.wms.pallet.infrastructure.mapper.PalletMapper;
import com.supplychain.wms.pallet.infrastructure.repository.PalletRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PalletServiceImpl implements PalletService {

    private final PalletRepository palletRepository;
    private final PalletMapper palletMapper;

    @Override
    @Transactional
    public PalletResponseDto createPallet(CreatePalletRequestDto request) {
        log.info("Creating Pallet record: type={}", request.getPalletType());

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            if (palletRepository.existsByBarcodeAndDeletedFalse(request.getBarcode().trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode already exists: " + request.getBarcode());
            }
        }

        Pallet pallet = palletMapper.toEntity(request);
        pallet.setPalletNumber(generatePalletNumber());
        pallet.setPalletStatus(PalletStatus.AVAILABLE);
        pallet.setActive(true);
        pallet.setDeleted(false);

        Pallet saved = palletRepository.save(pallet);
        return palletMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PalletResponseDto findById(Long id) {
        Pallet pallet = palletRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with ID: " + id));
        return palletMapper.toDto(pallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PalletResponseDto> findAll(String search, Pageable pageable) {
        Specification<Pallet> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("palletNumber")), pattern),
                        cb.like(cb.lower(root.get("lpnNumber")), pattern),
                        cb.like(cb.lower(root.get("barcode")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Pallet> page = palletRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(palletMapper::toDto));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Pallet pallet = palletRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with ID: " + id));
        pallet.setDeleted(true);
        palletRepository.save(pallet);
    }
}
```

---

### 3.3 Repository Conventions
1. Extend `JpaRepository<Entity, Long>` and `JpaSpecificationExecutor<Entity>`.
2. Annotate with `@Repository`.
3. Method names must include `AndDeletedFalse` for soft-delete safety.
4. For locking queries, use `@Lock(LockModeType.PESSIMISTIC_WRITE)` or custom JPQL with `FOR UPDATE`.

```java
package com.supplychain.wms.pallet.infrastructure.repository;

import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.model.Pallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PalletRepository extends JpaRepository<Pallet, Long>, JpaSpecificationExecutor<Pallet> {

    Optional<Pallet> findByIdAndDeletedFalse(Long id);
    Optional<Pallet> findByPalletNumberAndDeletedFalse(String palletNumber);
    Optional<Pallet> findByLpnNumberAndDeletedFalse(String lpnNumber);
    boolean existsByPalletNumberAndDeletedFalse(String palletNumber);
    boolean existsByBarcodeAndDeletedFalse(String barcode);
    List<Pallet> findAllByPalletStatusAndActiveTrueAndDeletedFalse(PalletStatus palletStatus);
}
```

---

### 3.4 Entity & DTO Design

#### 3.4.1 BaseEntity Standard (`com.supplychain.common.domain.BaseEntity`)
Every entity in the system MUST extend `BaseEntity`:
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id`
- `Long orgId` (`org_id`)
- `Long dcId` (`dc_id`)
- `String status` (`status`)
- `String remarks` (`remarks`)
- `@CreatedBy String createdBy` (`created_by`, `updatable = false`)
- `@CreatedDate LocalDateTime createdDate` (`created_date`, `updatable = false`)
- `@LastModifiedBy String modifiedBy` (`modified_by`)
- `@LastModifiedDate LocalDateTime modifiedDate` (`modified_date`)
- `@Builder.Default boolean deleted = false` (`deleted`)

#### 3.4.2 Entity Definition Pattern
```java
package com.supplychain.wms.pallet.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.wms.pallet.domain.enums.PalletMaterial;
import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.enums.PalletType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "pallets")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Pallet extends BaseEntity {

    @Column(name = "pallet_number", nullable = false, length = 50)
    private String palletNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "pallet_type", nullable = false, length = 50)
    private PalletType palletType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pallet_status", nullable = false, length = 50)
    private PalletStatus palletStatus;

    @Column(name = "max_weight", precision = 19, scale = 4)
    private BigDecimal maxWeight;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
```

---

### 3.5 Mapper Conventions (MapStruct 1.6.3)
1. Use MapStruct interfaces with `@Mapper(componentModel = "spring")`.
2. Define `toDto(Entity entity)`, `toDtoList(List<Entity> entities)`, `toEntity(CreateRequestDto dto)`.
3. For updates, use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` with `void updateEntity(UpdateRequestDto dto, @MappingTarget Entity entity)`.
4. Always ignore unmapped entity base/system fields (`id`, `version`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`, `deleted`).

```java
package com.supplychain.wms.pallet.infrastructure.mapper;

import com.supplychain.wms.pallet.application.dto.CreatePalletRequestDto;
import com.supplychain.wms.pallet.application.dto.PalletResponseDto;
import com.supplychain.wms.pallet.application.dto.UpdatePalletRequestDto;
import com.supplychain.wms.pallet.domain.model.Pallet;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PalletMapper {

    PalletResponseDto toDto(Pallet entity);
    List<PalletResponseDto> toDtoList(List<Pallet> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Pallet toEntity(CreatePalletRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdatePalletRequestDto dto, @MappingTarget Pallet entity);
}
```

---

## 4. Database Guidelines (PostgreSQL & Flyway)

### 4.1 Migration Protection Rules (CRITICAL)
- **DO NOT modify, rename, delete, or reorder existing migrations (`V1` through `V21`).**
- Any new database changes must be added as a **new, sequential Flyway migration file**:
  `src/main/resources/db/migration/V<NEXT_NUMBER>__<Descriptive_Name>.sql` (e.g. `V22__Create_New_Feature_Schema.sql`).

### 4.2 Schema Conventions
1. **Schema Name**: Always specify `scm` (`CREATE TABLE scm.<table_name>`).
2. **Table Naming**: Lowercase plural snake_case (e.g., `scm.pallets`, `scm.asn_headers`, `scm.asn_lines`, `scm.purchase_orders`).
3. **Column Naming**: Lowercase snake_case (e.g., `pallet_number`, `pallet_type`, `max_weight`).
4. **Required Base Columns**:
   ```sql
   id BIGSERIAL PRIMARY KEY,
   org_id BIGINT,
   dc_id BIGINT,
   status VARCHAR(50),
   remarks VARCHAR(500),
   created_by VARCHAR(100),
   created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   modified_by VARCHAR(100),
   modified_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   deleted BOOLEAN NOT NULL DEFAULT FALSE
   ```
5. **Indexes & Constraints**:
   - Create explicit indexes on foreign keys and frequently searched codes:
     `CREATE INDEX idx_pallets_pallet_number ON scm.pallets(pallet_number);`
   - Include `deleted = FALSE` partial unique index where uniqueness is soft-delete aware:
     `CREATE UNIQUE INDEX uq_pallets_barcode_active ON scm.pallets(barcode) WHERE deleted = FALSE AND barcode IS NOT NULL;`

---

## 5. Reusable Code & Best Practice Synergies

### 5.1 Shared Architecture Components
| Component | Class / Location | Purpose |
| :--- | :--- | :--- |
| **API Envelope** | `com.supplychain.common.application.dto.ApiResponse<T>` | Standardized JSON payload wrapping (`success`, `message`, `data`) |
| **Paginated Response** | `com.supplychain.common.application.dto.PageResponse<T>` | Standard pagination metadata (`content`, `page`, `size`, `totalElements`, `totalPages`) |
| **Dropdown DTO** | `com.supplychain.common.application.dto.DropdownOptionDto` | Universal key-value option model for UI select inputs (`id`, `code`, `label`, `description`) |
| **Base Entity** | `com.supplychain.common.domain.BaseEntity` | Audit timestamps, user attribution, tenant IDs (`orgId`, `dcId`), and soft-deletion flag |
| **Base DTO** | `com.supplychain.common.application.dto.BaseDto` | Mirror DTO for base entity fields |
| **Exception Advice** | `com.supplychain.common.exception.GlobalExceptionHandler` | Centralized translation of `ResponseStatusException` and validation errors to `ApiResponse.error()` |

### 5.2 Reusable Number Generation Pattern
Standardize sequential entity identification (inspired by Yard360 best practices):
```java
// Pattern: <PREFIX>-<yyyyMMdd>-<5-digit-sequence>
private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
private final AtomicLong seq = new AtomicLong(System.currentTimeMillis() % 100000);

private String generateFormattedNumber(String prefix) {
    String datePart = LocalDate.now().format(DATE_FMT);
    long nextVal = seq.incrementAndGet() % 100000;
    return String.format("%s-%s-%05d", prefix, datePart, nextVal);
}
```

---

## 6. Testing Guidelines

### 6.1 Standards
- **Framework**: JUnit 5 (`org.junit.jupiter.api.Test`), Spring Boot Test (`@SpringBootTest`).
- **Mocking**: `@MockBean` / `@Mock` with Mockito (`when(...).thenReturn(...)`).
- **Naming**: `*Test.java` for unit tests, `*IntegrationTest.java` for end-to-end slice tests.
- **Assertions**: AssertJ (`assertThat(...)`) or JUnit assertions (`assertEquals(...)`).

---

## 7. Implementation Checklist

Before finishing any new feature or pull request in `supply-chain-management`, verify each item:

- [ ] **1. Architecture & Layering**:
  - [ ] Package created under appropriate module (`procurement`, `wms`, `outbound`, `tms`, or `catalog`).
  - [ ] Structure follows `application/dto`, `application/service`, `domain/model`, `domain/enums`, `infrastructure/mapper`, `infrastructure/repository`, `presentation/controller`.
- [ ] **2. Database Migration**:
  - [ ] New migration created sequentially (e.g. `V22__Create_<Feature>_Schema.sql`).
  - [ ] Schema specifies `scm.` prefix.
  - [ ] Base audit columns (`created_by`, `created_date`, `modified_by`, `modified_date`, `deleted`) included.
  - [ ] **Verified migrations `V1` through `V21` were untouched.**
- [ ] **3. Entity & DTOs**:
  - [ ] Entity extends `BaseEntity` with `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
  - [ ] DTOs created for Create, Update, and Response views.
  - [ ] Jakarta validation annotations (`@NotBlank`, `@NotNull`, `@Size`, `@Positive`) added on request DTOs.
- [ ] **4. Mapper (MapStruct)**:
  - [ ] Mapper interface created with `@Mapper(componentModel = "spring")`.
  - [ ] Update mapping uses `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`.
  - [ ] Base audit and version fields ignored in mappings.
- [ ] **5. Repository**:
  - [ ] Repository extends `JpaRepository<Entity, Long>` and `JpaSpecificationExecutor<Entity>`.
  - [ ] Soft-delete safe methods (`...AndDeletedFalse`) defined.
- [ ] **6. Service**:
  - [ ] Interface + Impl created with `@Service`, `@Slf4j`, `@RequiredArgsConstructor`.
  - [ ] `@Transactional` applied appropriately.
  - [ ] Throws `ResponseStatusException` with standard `HttpStatus` on errors.
- [ ] **7. Controller**:
  - [ ] REST Controller created with `@RestController`, `@RequestMapping("/api/<resources>")`.
  - [ ] Uses `@Valid` on request payloads.
  - [ ] Returns `ResponseEntity<ApiResponse<T>>` or `ResponseEntity<Void>`.
- [ ] **8. Verification**:
  - [ ] Application compiles successfully with `./gradlew build -x test`.
