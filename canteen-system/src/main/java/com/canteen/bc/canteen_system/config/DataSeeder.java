package com.canteen.bc.canteen_system.config;

import com.canteen.bc.canteen_system.entity.*;
import com.canteen.bc.canteen_system.model.DataType;
import com.canteen.bc.canteen_system.model.Role;
import com.canteen.bc.canteen_system.model.UserType;
import com.canteen.bc.canteen_system.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private SysConfigRepository sysConfigRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final Map<String, String> ITEM_IMAGES = Map.of(
        "宮保雞丁",   "https://www.ytower.com.tw/recipe/iframe-recipe.asp?seq=A03-2165",
        "麻婆豆腐",     "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTMBxUxojjXinmAqAp4OoT6_fbZQoZ5Ag_ZsA&s",
        "炒飯",  "https://oceans-nadia.com/user/253470/recipe/440737?utm_source=site&utm_medium=r_card&utm_campaign=recipe_card_253470_440737",
        "蒸魚",          "https://www.ytower.com.tw/recipe/iframe-recipe.asp?seq=B01-1529",
        "綠茶",  "https://medias.pns.hk/publishing/PNSHK-147644-front-zoom.jpg?version=1763468507&imageresize=737_737",
        "奶茶",           "https://www.healingdaily.com.tw/media/article/00/02/256_e446a6586dbf.jpeg",
        "豆漿",            "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Rice_vermicelli.jpg/400px-Rice_vermicelli.jpg",
        "Teh Tarik",            "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Teh_tarik_man_pulling_tea.jpg/400px-Teh_tarik_man_pulling_tea.jpg",
        "Milo Ais",             "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Australian_milo.jpg/400px-Australian_milo.jpg",
        "檸檬茶",          "https://www.vitavitasoy.com/tc/product/vlt/vlt"
    );

    @Override
    @Transactional
    public void run(String... args) {
        updateImageUrls(); // always runs — safe to call on existing databases

        if (userRepository.existsBySchoolId("ADMIN001")) return; // already seeded

        // ── Users ────────────────────────────────────────────────────────────
        UserEntity admin = userRepository.save(UserEntity.builder()
                .schoolId("ADMIN001").name("Canteen Admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN).userType(UserType.STAFF).build());

        UserEntity stu1 = userRepository.save(UserEntity.builder()
                .schoolId("STU001").name("Ahmad Razif")
                .password(passwordEncoder.encode("student123"))
                .role(Role.CUSTOMER).userType(UserType.STUDENT).build());

        UserEntity stu2 = userRepository.save(UserEntity.builder()
                .schoolId("STU002").name("Siti Nurbaya")
                .password(passwordEncoder.encode("student123"))
                .role(Role.CUSTOMER).userType(UserType.STUDENT).build());

        UserEntity staff1 = userRepository.save(UserEntity.builder()
                .schoolId("STAFF001").name("Encik Hafiz")
                .password(passwordEncoder.encode("staff123"))
                .role(Role.CUSTOMER).userType(UserType.STAFF).build());

        UserEntity kitchen1 = userRepository.save(UserEntity.builder()
                .schoolId("KITCHEN001").name("Kak Rosmah")
                .password(passwordEncoder.encode("kitchen123"))
                .role(Role.KITCHEN).userType(UserType.STAFF).build());

        // ── Wallets ──────────────────────────────────────────────────────────
        walletRepository.save(WalletEntity.builder().user(admin).balance(new BigDecimal("100.00")).build());
        walletRepository.save(WalletEntity.builder().user(stu1).balance(new BigDecimal("25.50")).build());
        walletRepository.save(WalletEntity.builder().user(stu2).balance(new BigDecimal("8.00")).build());
        walletRepository.save(WalletEntity.builder().user(staff1).balance(new BigDecimal("50.00")).build());
        walletRepository.save(WalletEntity.builder().user(kitchen1).balance(new BigDecimal("0.00")).build());

        // ── Items ────────────────────────────────────────────────────────────
        ItemEntity nasiLemak = itemRepository.save(ItemEntity.builder()
                .name("Nasi Lemak Special").description("Fragrant coconut rice with sambal, egg, anchovies & peanuts")
                .price(new BigDecimal("4.50")).imageUrl(ITEM_IMAGES.get("Nasi Lemak Special")).isVisible(true).build());

        ItemEntity meeGoreng = itemRepository.save(ItemEntity.builder()
                .name("Mee Goreng Mamak").description("Stir-fried yellow noodles with egg, tofu & tomato")
                .price(new BigDecimal("4.00")).imageUrl(ITEM_IMAGES.get("Mee Goreng Mamak")).isVisible(true).build());

        ItemEntity nasiGoreng = itemRepository.save(ItemEntity.builder()
                .name("Nasi Goreng Kampung").description("Village-style fried rice with anchovies & vegetables")
                .price(new BigDecimal("4.50")).imageUrl(ITEM_IMAGES.get("Nasi Goreng Kampung")).isVisible(true).build());

        ItemEntity ayamGoreng = itemRepository.save(ItemEntity.builder()
                .name("Ayam Goreng").description("Crispy southern-fried chicken, served with chilli sauce")
                .price(new BigDecimal("5.50")).imageUrl(ITEM_IMAGES.get("Ayam Goreng")).isVisible(true).build());

        ItemEntity nasiLauk = itemRepository.save(ItemEntity.builder()
                .name("Nasi + Lauk Pilihan").description("White rice with your choice of 1 main dish + 2 sides")
                .price(new BigDecimal("5.00")).imageUrl(ITEM_IMAGES.get("Nasi + Lauk Pilihan")).isVisible(true).build());

        ItemEntity rotiCanai = itemRepository.save(ItemEntity.builder()
                .name("Roti Canai").description("Flaky flatbread served with dhal & curry sauce")
                .price(new BigDecimal("1.80")).imageUrl(ITEM_IMAGES.get("Roti Canai")).isVisible(true).build());

        ItemEntity mihunSoup = itemRepository.save(ItemEntity.builder()
                .name("Mihun Sup").description("Rice vermicelli in clear chicken broth with vegetables")
                .price(new BigDecimal("3.50")).imageUrl(ITEM_IMAGES.get("Mihun Sup")).isVisible(true).build());

        ItemEntity tehTarik = itemRepository.save(ItemEntity.builder()
                .name("Teh Tarik").description("Frothy pulled milk tea, hot or iced")
                .price(new BigDecimal("1.80")).imageUrl(ITEM_IMAGES.get("Teh Tarik")).isVisible(true).build());

        ItemEntity miloIce = itemRepository.save(ItemEntity.builder()
                .name("Milo Ais").description("Chilled Milo chocolate malt drink")
                .price(new BigDecimal("2.00")).imageUrl(ITEM_IMAGES.get("Milo Ais")).isVisible(true).build());

        ItemEntity airPutih = itemRepository.save(ItemEntity.builder()
                .name("Air Mineral").description("500ml mineral water")
                .price(new BigDecimal("1.00")).imageUrl(ITEM_IMAGES.get("Air Mineral")).isVisible(true).build());

        // ── Menus ────────────────────────────────────────────────────────────
        MenuEntity breakfast = menuRepository.save(MenuEntity.builder()
                .name("Breakfast").isActive(true).build());

        MenuEntity lunch = menuRepository.save(MenuEntity.builder()
                .name("Lunch").isActive(true).build());

        // ── Menu–Item links ──────────────────────────────────────────────────
        for (ItemEntity item : List.of(rotiCanai, mihunSoup, tehTarik, miloIce, airPutih)) {
            menuItemRepository.save(MenuItemEntity.builder()
                    .menuEntity(breakfast).itemEntity(item).build());
        }

        for (ItemEntity item : List.of(nasiLemak, meeGoreng, nasiGoreng, ayamGoreng, nasiLauk, tehTarik, miloIce, airPutih)) {
            menuItemRepository.save(MenuItemEntity.builder()
                    .menuEntity(lunch).itemEntity(item).build());
        }

        // ── Cut-off time (legacy key kept for backward compat) ───────────────
        if (sysConfigRepository.findByConfigKey("LunchCutOffKey").isEmpty()) {
            sysConfigRepository.save(SysConfigEntity.builder()
                    .configKey("LunchCutOffKey").configValue("11:30:00")
                    .dataType(DataType.TIME).description("Daily lunch order cut-off time").build());
        }

        // ── Order window (open + close times) ────────────────────────────────
        if (sysConfigRepository.findByConfigKey("OrderWindowOpen").isEmpty()) {
            sysConfigRepository.save(SysConfigEntity.builder()
                    .configKey("OrderWindowOpen").configValue("07:30:00")
                    .dataType(DataType.TIME).description("Ordering window open time").build());
        }
        if (sysConfigRepository.findByConfigKey("OrderWindowClose").isEmpty()) {
            sysConfigRepository.save(SysConfigEntity.builder()
                    .configKey("OrderWindowClose").configValue("11:30:00")
                    .dataType(DataType.TIME).description("Ordering window close time").build());
        }

        System.out.println("[DataSeeder] Seed complete — ADMIN001/admin123, STU001/student123, STU002/student123, STAFF001/staff123, KITCHEN001/kitchen123");
    }

    private void updateImageUrls() {
        ITEM_IMAGES.forEach((name, url) ->
            itemRepository.findByName(name).ifPresent(item -> {
                item.setImageUrl(url);
                itemRepository.save(item);
            })
        );
    }
}
