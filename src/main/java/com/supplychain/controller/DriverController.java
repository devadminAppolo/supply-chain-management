package com.supplychain.controller;

import com.supplychain.dto.DriverDto;
import com.supplychain.service.DriverService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public String drivers(Model model) {

        model.addAttribute("driver", new DriverDto());
        model.addAttribute("drivers", driverService.findAll());

        return "drivers";
    }

    @PostMapping
    public String save(@ModelAttribute DriverDto dto) {

        driverService.save(dto);

        return "redirect:/drivers";
    }
}
