package com.javarush.jira.profile.internal.web;


import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.common.error.IllegalRequestDataException;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.User;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
class ProfileRestControllerTest extends AbstractControllerTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileRestController profileRestController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGet() throws Exception {

        long testId = 1L;
        // настройка AuthUser для profileRestController.get()
        Set<Role> roles = Set.of(Role.ADMIN, Role.DEV, Role.MANAGER);
        User user = new User();
        user.setId(testId);
        user.setEmail("testemail@gmail.com");
        user.setPassword("amazingPassword");
        user.setRoles(roles);
        AuthUser authUser = new AuthUser(user);

        // настройка Profile для profileRepository.getOrCreate()
        Profile profile = new Profile();
        profile.setId(testId);
        profile.setLastLogin(LocalDateTime.now());
        profile.setLastFailedLogin(LocalDateTime.now());
        profile.setMailNotifications(1L);

        // настройка ProfileTo для profileMapper.toTo(), который уйдет нашему тестируемому методу profileRestController.get()
        ProfileTo profileTo = new ProfileTo(testId, null, null);

        // настройка mockito возвратов
        when(profileRepository.getOrCreate(testId)).thenReturn(profile);
        when(profileMapper.toTo(profile)).thenReturn(profileTo);

        // вызываю наш тестируемый метод
        ProfileTo result = profileRestController.get(authUser);

        // проверяю
        assertEquals(profileTo, result);
        verify(profileRepository, times(1)).getOrCreate(testId);
        verify(profileMapper, times(1)).toTo(profile);
    }

    @Test
    public void testGetWhenIdNull() throws Exception {

        // настройка AuthUser для profileRestController.get()
        Set<Role> roles = Set.of(Role.ADMIN, Role.DEV, Role.MANAGER);
        User user = new User();
        user.setId(null);
        user.setEmail("testemail@gmail.com");
        user.setPassword("amazingPassword");
        user.setRoles(roles);
        AuthUser authUser = new AuthUser(user);

        // вызываю наш тестируемый метод и проверяю совпадение с ошибкой
        assertThrows(IllegalArgumentException.class, () -> {
            profileRestController.get(authUser.id());
        });

        try {
            profileRestController.get(authUser.id());
            fail();
        } catch (IllegalArgumentException iae) {
            String message = "Entity must have id";
            assertEquals(message, iae.getMessage());
        }
    }

    @Test
    public void testUpdateProfile() throws Exception {
        long testId = 1L;
        // настройка ProfileTo для profileRestController.update()
        // настройка ProfileTo для ValidationUtil.assureIdConsistent()
        // настройка ProfileTo для profileRepository.getOrCreate()
        ContactTo contact1 = new ContactTo();
        contact1.setId(testId);
        contact1.setCode("skype");
        contact1.setValue("userSkype");
        Set<ContactTo> contacts = Set.of(contact1);
        ProfileTo profileTo = new ProfileTo(testId, null, contacts);

        // настройка AuthUser для profileRestController.update
        Set<Role> roles = Set.of(Role.ADMIN, Role.DEV, Role.MANAGER);
        User user = new User();
        user.setId(testId);
        user.setEmail("fknshitbro@goddamn.com");
        user.setPassword("amazingPassword");
        user.setRoles(roles);
        AuthUser authUser = new AuthUser(user);

        // настройка Profile для profileRepository.getOrCreate()
        Profile profile = new Profile();
        profile.setId(testId);
        profile.setLastLogin(LocalDateTime.now());
        profile.setLastFailedLogin(LocalDateTime.now());
        profile.setMailNotifications(1L);

        // настройка mockito возвратов
        when(profileRepository.getOrCreate(profileTo.id())).thenReturn(profile);
        when(profileMapper.updateFromTo(profile, profileTo)).thenReturn(profile);

        // вызываю наш тестируемый метод
        profileRestController.update(profileTo, authUser);

        // проверяю
        verify(profileRepository, times(1)).getOrCreate(profileTo.id());
        verify(profileMapper, times(1)).updateFromTo(profile, profileTo);
        verify(profileRepository, times(1)).save(profile);
    }

    @Test
    public void testUpdateProfileErrorWhenIdsNotEquals() throws Exception {
        // настройка ProfileTo для profileRestController.update()
        // настройка ProfileTo для ValidationUtil.assureIdConsistent()
        // настройка ProfileTo для profileRepository.getOrCreate()
        ContactTo contact1 = new ContactTo();
        contact1.setId(1L);
        contact1.setCode("skype");
        contact1.setValue("userSkype");
        Set<ContactTo> contacts = Set.of(contact1);
        ProfileTo profileTo = new ProfileTo(1L, null, contacts);

        // настройка AuthUser с другим id для profileRestController.update, чтобы это вызвало ошибку
        Set<Role> roles = Set.of(Role.ADMIN, Role.DEV, Role.MANAGER);
        User user = new User();
        user.setId(2L);
        user.setEmail("fknshitbro@goddamn.com");
        user.setPassword("amazingPassword");
        user.setRoles(roles);
        AuthUser authUser = new AuthUser(user);

        // вызываю наш тестируемый метод и проверяю совпадение с ошибкой
        assertThrows(IllegalRequestDataException.class, () -> {
            profileRestController.update(profileTo, authUser);
        });

        // проверяю сообщение ошибки
        try {
            profileRestController.update(profileTo, authUser);
            fail();
        } catch (IllegalRequestDataException irde) {
            String message = profileTo.getClass().getSimpleName() + " must has id=" + authUser.id();
            assertEquals(message, irde.getMessage());
        }
    }


}