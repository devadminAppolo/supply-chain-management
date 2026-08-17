package com.supplychain.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Lightweight DTO returned by all /dropdown endpoints.
 *
 * <pre>
 * {
 *   "name":  "Main Warehouse",   // displayed in the UI select/combo
 *   "value": "Main Warehouse"    // submitted as the selected key in the payload
 * }
 * </pre>
 *
 * Both {@code name} and {@code value} carry the same human-readable string so that
 * the user sees a meaningful value in the UI instead of a raw database ID.
 * {@code @EqualsAndHashCode} enables {@link java.util.stream.Stream#distinct()} to
 * deduplicate entries when multiple active records share the same display name.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class DropdownOptionDto {
    private final String name;
    private final String value;
}
