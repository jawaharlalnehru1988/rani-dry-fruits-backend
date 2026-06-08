package com.asknehru.myclientsapi.core.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;

    public AdminUserInitializer(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public void run(String... args) {
        if (adminUserRepository.count() == 0) {
            AdminUser admin1 = new AdminUser();
            admin1.setUsername("HareKrishna");
            admin1.setPassword("8248200472");
            adminUserRepository.save(admin1);

            AdminUser admin2 = new AdminUser();
            admin2.setUsername("Ganeshan");
            admin2.setPassword("RaniGanesan123");
            adminUserRepository.save(admin2);

            System.out.println("Default admin users initialized successfully.");
        }
    }
}
