package com.mztrade.hki.controller;


import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.entity.ChartSetting;
import com.mztrade.hki.service.ChartSettingService;
import com.mztrade.hki.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class UserController {

    private final ChartSettingService chartSettingService;
    private UserService userService;

    @Autowired
    public UserController(UserService userService, ChartSettingService chartSettingService) {
        this.userService = userService;
        this.chartSettingService = chartSettingService;
    }

    @PostMapping("/user")
    public ResponseEntity<Integer> saveUser(
            @RequestParam String firebaseUid,
            @RequestParam String name
    ) {
        log.info(String.format("[POST] /saveUser?firebaseUid=%s has been called.", firebaseUid));
        return new ResponseEntity<>(
                userService.saveUser(
                        UserDto.builder()
                                .firebaseUid(firebaseUid)
                                .name(name)
                                .build()
                ), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDto> findUser(
            @RequestParam String firebaseUid
    ) {
        log.info(String.format("[GET] /user?firebaseUid=%s has been called.", firebaseUid));
        try {
            return new ResponseEntity<>(userService.findUser(firebaseUid), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @GetMapping("/user/duplicate-check/{str}")
    public ResponseEntity<Boolean> duplicateCheck(
            @PathVariable String str
    ) {
        log.info(String.format("[GET] /user/duplicate-check/%s has been called.", str));
        return new ResponseEntity<>(userService.isEmailExists(str) || userService.isNameExists(str), HttpStatus.OK);
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(
            @RequestParam String firebaseUid
    ) {
        log.info(String.format("[DELETE-XXX] /deleteUser?firebaseUid=%s has been called.", firebaseUid));
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/user/{uid}/chart-setting")
    public ChartSetting getChartSetting(@PathVariable int uid) {
        ChartSetting chartSetting = chartSettingService.get(uid);
        log.info(String.format("[GET] /user/%s/chart-setting", uid));
        return chartSetting;
    }

    @PutMapping("/user/{uid}/chart-setting")
    public boolean saveChartSetting(@PathVariable int uid, @RequestParam String indicator) {
        ChartSetting chartSetting = ChartSetting.builder().uid(uid).indicator(indicator).build();
        boolean update = chartSettingService.save(chartSetting);
        log.info(String.format("[PUT] /user/%s/chart-setting?indicator=%s -> update:%b", uid,
                indicator, update));
        return update;
    }
}
