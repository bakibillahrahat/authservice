package org.mbr.authservice.controller;

import lombok.AllArgsConstructor;

import java.util.Objects;

import org.mbr.authservice.dtos.JwtResponseDTO;
import org.mbr.authservice.entities.RefreshToken;
import org.mbr.authservice.model.UserInfoDto;
import org.mbr.authservice.services.JwtService;
import org.mbr.authservice.services.RefreshTokenService;
import org.mbr.authservice.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class AuthController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/auth/v1/signup")
    public ResponseEntity SignUp(@RequestBody UserInfoDto userInfoDto){
        try{
            String userId = userDetailsService.signupUser(userInfoDto);
            if(Objects.isNull(userId)){
                return new ResponseEntity<>("Already exist", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUsername());
            return new ResponseEntity<>(JwtResponseDTO.builder().accessToken(jwtToken).token(refreshToken.getToken()).userId(userId).build(), HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(ex.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   @GetMapping("/auth/v1/ping")
    public ResponseEntity<String> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = userDetailsService.getUserByUsername(authentication.getName());
            if(Objects.nonNull(userId)){
                return ResponseEntity.ok(userId);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    @GetMapping("/auth/v1/health")
    public ResponseEntity<Boolean> checkHealth(){
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
