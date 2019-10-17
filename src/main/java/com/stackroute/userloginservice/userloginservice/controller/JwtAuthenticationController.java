package com.stackroute.userloginservice.userloginservice.controller;


import java.util.Objects;

import com.stackroute.userloginservice.userloginservice.config.JwtTokenUtil;
import com.stackroute.userloginservice.userloginservice.exception.EmailIdAlreadyTakenException;
import com.stackroute.userloginservice.userloginservice.exception.UserNameAlreadyTakenException;
import com.stackroute.userloginservice.userloginservice.model.*;
import com.stackroute.userloginservice.userloginservice.repository.UserDao;
import com.stackroute.userloginservice.userloginservice.service.JwtUserDetailsService;
import com.stackroute.userloginservice.userloginservice.service.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    RabbitMQSender rabbitMQSender;

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        rabbitMQSender.sendToken(token);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody UserDTO user) throws RuntimeException {
        DAOUser daoUser1 = userDao.findByUsername(user.getUsername());
        DAOUser daoUser2 = userDao.findByEmailId(user.getEmailId());
        if (daoUser1 != null) {
            throw new UserNameAlreadyTakenException("Username Not Available" );
        }
        else if (daoUser2 != null) {
            throw new EmailIdAlreadyTakenException("There is an account linked with this email id");
        }
        else {
            MessageUser messageUser = new MessageUser(user.getUsername(), user.getName(), user.getEmailId());
            rabbitMQSender.sendUser(messageUser);
            return ResponseEntity.ok(userDetailsService.save(user));
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
