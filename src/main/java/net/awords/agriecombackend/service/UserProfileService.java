package net.awords.agriecombackend.service;

import java.util.List;
import java.util.stream.Collectors;
import net.awords.agriecombackend.dto.user.UserDtos;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.entity.UserAddress;
import net.awords.agriecombackend.repository.UserAddressRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 用户资料、地址管理服务。
 */
@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    public UserProfileService(UserRepository userRepository,
                              UserAddressRepository userAddressRepository) {
        this.userRepository = userRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @Transactional(readOnly = true)
    public UserDtos.ProfileResponse getProfile(String username) {
        User user = loadUser(username);
        return toProfileResponse(user);
    }

    @Transactional
    public UserDtos.ProfileResponse updateProfile(String username, UserDtos.UpdateProfileRequest request) {
        User user = loadUser(username);
        if (request.nickname != null) {
            user.setNickname(request.nickname);
        }
        if (request.avatarUrl != null) {
            user.setAvatarUrl(request.avatarUrl);
        }
        if (request.email != null) {
            user.setEmail(request.email);
        }
        if (request.phone != null) {
            user.setPhone(request.phone);
        }
        if (request.bio != null) {
            user.setBio(request.bio);
        }
        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserDtos.AddressResponse> listAddresses(String username) {
        User user = loadUser(username);
        return userAddressRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDtos.AddressResponse createAddress(String username, UserDtos.CreateAddressRequest request) {
        User user = loadUser(username);
        if (Boolean.TRUE.equals(request.isDefault)) {
            userAddressRepository.resetDefaultForUser(user.getId());
        }
        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setRecipientName(request.recipientName);
        address.setPhone(request.phone);
        address.setProvince(request.province);
        address.setCity(request.city);
        address.setDistrict(request.district);
        address.setStreet(request.street);
        address.setPostalCode(request.postalCode);
        address.setDefaultAddress(Boolean.TRUE.equals(request.isDefault));
        userAddressRepository.save(address);
        return toAddressResponse(address);
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    private UserDtos.ProfileResponse toProfileResponse(User user) {
        UserDtos.ProfileResponse profile = new UserDtos.ProfileResponse();
        profile.id = user.getId();
        profile.username = user.getUsername();
        profile.nickname = user.getNickname();
        profile.avatarUrl = user.getAvatarUrl();
        profile.email = user.getEmail();
        profile.phone = user.getPhone();
        profile.bio = user.getBio();
        profile.createdAt = user.getCreatedAt();
        profile.updatedAt = user.getUpdatedAt();
        profile.roles = user.getRoles().stream().map(Role::getName).toArray(String[]::new);
        return profile;
    }

    private UserDtos.AddressResponse toAddressResponse(UserAddress address) {
        UserDtos.AddressResponse dto = new UserDtos.AddressResponse();
        dto.id = address.getId();
        dto.recipientName = address.getRecipientName();
        dto.phone = address.getPhone();
        dto.province = address.getProvince();
        dto.city = address.getCity();
        dto.district = address.getDistrict();
        dto.street = address.getStreet();
        dto.postalCode = address.getPostalCode();
        dto.isDefault = address.isDefaultAddress();
        dto.createdAt = address.getCreatedAt();
        dto.updatedAt = address.getUpdatedAt();
        return dto;
    }
}
