package com.mztrade.hki.repo;

import com.mztrade.hki.repository.TagRepository;
import com.mztrade.hki.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TagTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TagRepository tagRepository;

    @Transactional
    @Test
    void saveAndDelete() {
        /*User user = userRepository.save(User.builder().name("test11").password("test1").build());
        System.out.println(user);

        Tag tag = tagRepository.save(
                Tag.builder().user(user).tname("TestTag").tcolor("0xFF123456").category(1).build()
        );

        System.out.println(tag);
        System.out.println(user.getTags());

        User refreshedUser = userRepository.getReferenceById(1);
        System.out.println(refreshedUser.getTags());*/
    }
}
