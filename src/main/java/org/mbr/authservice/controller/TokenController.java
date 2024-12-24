package org.mbr.authservice.controller;

import java.util.Objects;

import javax.management.RuntimeErrorException;

import org.mbr.authservice.dtos.AuthRequestDTO;
import org.mbr.authservice.dtos.JwtResponseDTO;
import org.mbr.authservice.dtos.RefreshTokenRequestDTO;
import org.mbr.authservice.entities.RefreshToken;
import org.mbr.authservice.services.JwtService;
import org.mbr.authservice.services.RefreshTokenService;
import org.mbr.authservice.services.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class TokenController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/auth/v1/login")
    public ResponseEntity AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        if(authentication.isAuthenticated()){
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            String userId = userDetailsService.getUserByUsername(authRequestDTO.getUsername());

            if(Objects.nonNull(userId) && Objects.nonNull(refreshToken)) {
                return new ResponseEntity<>(JwtResponseDTO.builder()
                        .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()))
                        .token(refreshToken.getToken())
                        .userId(userId)
                        .build(), HttpStatus.OK);
            }
        } 
        return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        
    }

    @PostMapping("/auth/v1/refreshtoken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        
        return refreshTokenService.findbyToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.GenerateToken(userInfo.getUsername());
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getToken())
                            .build();
                }).orElseThrow(() -> new RuntimeErrorException(null, "Refresh Token is not in DB...!!"));

    }
}
