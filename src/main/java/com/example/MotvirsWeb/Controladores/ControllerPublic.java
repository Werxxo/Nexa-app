package com.example.MotvirsWeb.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public")
public class ControllerPublic {

    @GetMapping("/home/pasajero")
    public String apartadodelHome() {
        return "index";
    }

    @GetMapping("/home/conductor")
    public String aparatadoconductor(){
        return "home_conductor";
    }


}


