package org.inmods.orderprocessing.controller;

import javax.servlet.http.HttpServletRequest;
import org.inmods.orderprocessing.model.Order;
import org.inmods.orderprocessing.service.OrderService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class OrderController {
    Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    OrderService orderService;
    @Autowired
    RestTemplate restTemplate;
    @Value("${application.fleetmanager.url}")
    String fleetmanager_url;

    @RequestMapping
    public String getAllOrders(Model model){
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "list-orders";
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public String create(Order order)
    {
        orderService.save(order);
        return "redirect:/";
    }

    @RequestMapping(path = {"/edit", "/edit/{id}"})
    public String edit(Model model, @PathVariable("id") Optional<Long> id)
    {
        if (id.isPresent()) {
            Optional<Order> order = orderService.findById(id.get());
            model.addAttribute("order", order.get());
        } else {
            model.addAttribute("order", new Order());
        }
        return "add-edit-order";
    }

    @RequestMapping("/start/{id}")
    public String start(Model model, @PathVariable("id") Long id){
        Optional<Order> order = orderService.findById(id);
        Map m = restTemplate.postForObject(fleetmanager_url + "/job", order.get(), Map.class);

        order.get().setDistance((Double) m.get("dist"));
        orderService.save(order.get());
        logger.info("Vehicle {} Dist {}", m.get("vehicle"), m.get("dist"));
        return "redirect:/";
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        logger.error("Request: " + req.getRequestURL() + " raised " + ex);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error");
        return mav;
    }

}
