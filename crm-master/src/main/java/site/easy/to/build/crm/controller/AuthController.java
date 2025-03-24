package site.easy.to.build.crm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    public AuthController(UserService userService){
        this.userService = userService;
    }

    
    @GetMapping("/findidbyemail")
    public ResponseEntity<Integer> findidbyemail(@RequestParam String email) {
        User user = userService.findFirstByOrderByIdAsc();
        
        if (user.getEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.ok(user.getId());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
    }

    @PostMapping("/login/customer")
    public ResponseEntity<String> loginCustomer(@RequestBody User request) {
        // if ("customer".equals(request.getRole()) && "password123".equals(request.getPassword())) {
        //     return ResponseEntity.ok("Token_Customer_456");
        // }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
