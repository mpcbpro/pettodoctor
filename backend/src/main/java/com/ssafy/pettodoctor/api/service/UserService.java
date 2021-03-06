package com.ssafy.pettodoctor.api.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ssafy.pettodoctor.api.domain.Pet;
import com.ssafy.pettodoctor.api.domain.User;
import com.ssafy.pettodoctor.api.domain.UserCertification;
import com.ssafy.pettodoctor.api.repository.PetRepository;
import com.ssafy.pettodoctor.api.repository.UserCertificationRepository;
import com.ssafy.pettodoctor.api.repository.UserRepository;
import com.ssafy.pettodoctor.api.request.PetPostReq;
import com.ssafy.pettodoctor.api.request.UserChangeReq;
import com.ssafy.pettodoctor.api.request.UserCommonSignupPostReq;
import com.ssafy.pettodoctor.api.request.UserPasswordChangeReq;
import com.ssafy.pettodoctor.common.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    @Value("${profileImg.path}")
    private String uploadFolder;

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final UserCertificationRepository userCertificationRepository;
    private final PasswordUtil passwordUtil;

    @Transactional
    public User signup(UserCommonSignupPostReq signupInfo) throws Exception {
        byte[] salt = PasswordUtil.getNextSalt();
        String hash = new String(PasswordUtil.hash(signupInfo.getPassword().toCharArray(), salt));

        // ??????
        User user = User.createCommonUser(signupInfo.getEmail(),
                signupInfo.getName(), hash, signupInfo.getAddress(), salt);
        // ??????
        user.setTel(signupInfo.getTel());

        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new Exception("???????????? ??????????????????.");


        userRepository.save(user);

        if(signupInfo.getPets() != null)
            for(PetPostReq pet : signupInfo.getPets()){
                Pet p = Pet.createPet(pet.getName(), pet.getBirthDate(), pet.getSpecies(), pet.getWeight(), user);
                petRepository.save(p);
            }
        return user;
    }

    @Transactional
    public User oauthSignup(String email, String nickname){
        User user = User.createOauthUser(email, nickname);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public Boolean isDuplicated(String email) {
        User findUser = userRepository.findByEmail(email);

        if (findUser == null) {
            return false;
        } else return true;
    }

    @Transactional
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void deleteNowUser(User nowUser) {
        userRepository.delete(nowUser);
    }

    @Transactional
    public Optional<User> changeUser(Long userId, UserChangeReq urq) {
        User user = userRepository.findById(userId).get();
        if (urq.getName() != null) {
            user.setName(urq.getName());
        }
        if (urq.getAddress() != null) {
            user.setAddress(urq.getAddress());
        }
        if (urq.getTel() != null) {
            user.setTel(urq.getTel());
        }
        if (urq.getJoinDate() != null) {
            user.setJoinDate(urq.getJoinDate());
        }
        if (urq.getIsCertificated() != null) {
            user.setIsCertificated(urq.getIsCertificated());
        }
        return Optional.ofNullable(user);
    }

    @Transactional
    public boolean checkPassword(String inputPass, User user) {
        String hash = new String(PasswordUtil.hash(inputPass.toCharArray(), user.getSalt()));
        return user.getPassword().equals(hash);
    }

    @Transactional
    public boolean changePassword(Long id, UserPasswordChangeReq upcr) {
        User user = userRepository.findById(id).get();
        String hash = new String(PasswordUtil.hash(upcr.getPassword().toCharArray(), user.getSalt()));
        if(user.getPassword().equals(hash)){
            String newHash = new String(PasswordUtil.hash(upcr.getNewPassword().toCharArray(), user.getSalt()));
            user.setPassword(newHash);
            return true;
        }
        return false;
    }

    public String getUserPassByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user.getPassword();
    }

    @Transactional
    public User updateProfile(Long userId, MultipartFile multipartFile) {
        User user = userRepository.findById(userId).get();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String imageFileName = "user_" + user.getId() + "_" + Long.toString(System.currentTimeMillis()) + "." + FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        Path imageFilePath = Paths.get(uploadFolder + imageFileName);

        if (multipartFile.getSize() != 0) { // ????????? ????????? ???????????? ??????
            try {
                if (user.getProfileImgUrl() != null) { // ?????? ????????? ????????? ?????? ??????
                    File file = new File(uploadFolder + user.getProfileImgUrl());
                    file.delete(); // ?????? ?????? ??????
                }
                Files.write(imageFilePath, multipartFile.getBytes());
            } catch (Exception e) {
                    e.printStackTrace();
            }
            user.setProfileImgUrl(imageFileName);
        }

        return user;
    }

    @Transactional
    public void mailCertification(String certificationKey) {
        User user = userCertificationRepository.certificate(certificationKey).get();
        UserCertification uc = userCertificationRepository.findByKey(certificationKey).get();
        userCertificationRepository.delete(uc.getId());
        user.setIsCertificated(true);
    }

}
