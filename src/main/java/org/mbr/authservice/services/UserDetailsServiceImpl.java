package org.mbr.authservice.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbr.authservice.entities.UserInfo;
import org.mbr.authservice.eventProducer.UserInfoEvent;
import org.mbr.authservice.eventProducer.UserInfoProducer;
import org.mbr.authservice.model.UserInfoDto;
import org.mbr.authservice.repositories.UserRepository;
import org.mbr.authservice.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Data
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserInfoProducer userInfoProducer;

    // Load user by username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepository.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("could not found user..!!");
        }
        return new CustomUserDetails(user);
    }

    // check the user is already exist or not
    public UserInfo checkIfUserAlreadyExist(UserInfoDto userInfoDto) {
         return userRepository.findByUsername(userInfoDto.getUsername());
    }

    // signup the user
    public String signupUser(UserInfoDto userInfoDto) {
        // Define a function to check  if userEmail, password is correct
        ValidationUtil.ValidateUserAttributes(userInfoDto);
        userInfoDto.setPassword(passwordEncoder.encode(userInfoDto.getPassword()));
        if(Objects.nonNull(checkIfUserAlreadyExist(userInfoDto))){
            return null;
        }
        String userId = UUID.randomUUID().toString();
        UserInfo userInfo = new UserInfo(userId, userInfoDto.getUsername(), userInfoDto.getPassword(), new HashSet<>());
        userRepository.save(userInfo);
        // pushEventToQueue
        userInfoProducer.sendEventToKafka(userInfoEventToPublish(userInfoDto, userId));
        return userId;
    }

    // get user by username
    public String getUserByUsername(String username){
        return Optional.of(userRepository.findByUsername(username)).map(UserInfo::getUserId).orElse(null);
    }

    // User information event publish
       private UserInfoEvent userInfoEventToPublish(UserInfoDto userInfoDto, String userId){
        return UserInfoEvent.builder()
                .userId(userId)
                .firstName(userInfoDto.getUsername())
                .lastName(userInfoDto.getLastname())
                .email(userInfoDto.getEmail())
                .phoneNumber(userInfoDto.getPhoneNumber()).build();

    }
}