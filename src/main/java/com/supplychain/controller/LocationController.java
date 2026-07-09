package com.supplychain.controller;
import com.supplychain.dto.LocationDto;
import com.supplychain.service.LocationService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public String locations(Model model) {

        model.addAttribute("location", new LocationDto());
        model.addAttribute("locations", locationService.findAll());

        return "locations";
    }

    @PostMapping
    public String save(@ModelAttribute LocationDto dto) {

        locationService.save(dto);

        return "redirect:/locations";
    }

}