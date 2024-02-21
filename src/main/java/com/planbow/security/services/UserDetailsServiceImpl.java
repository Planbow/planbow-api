package com.planbow.security.services;

import com.planbow.documents.users.User;
import com.planbow.repository.UserApiRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {
    private UserApiRepository userApiRepository;

    @Autowired
    public void setUserApiRepository(UserApiRepository userApiRepository) {
        this.userApiRepository = userApiRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userApiRepository.getUser(username);
        if (user == null)
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        return UserDetailsImpl.build(user);
    }

}
