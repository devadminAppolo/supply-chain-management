package com.supplychain.controller;

import org.springframework.ui.Model;
import com.supplychain.dto.ShipmentDto;
import com.supplychain.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public String list(Model model) {

        model.addAttribute("shipment", new ShipmentDto());
        model.addAttribute("shipments", shipmentService.findAll());

        return "shipments";
    }

    @PostMapping
    public String save(@ModelAttribute ShipmentDto dto) {

        shipmentService.save(dto);

        return "redirect:/shipments";
    }
}
