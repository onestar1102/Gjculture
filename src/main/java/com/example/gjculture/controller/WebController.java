package com.example.gjculture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/place")
    public String placeDetail(Model model, String id) {
        model.addAttribute("placeId", id);
        return "place-detail";
    }
}
