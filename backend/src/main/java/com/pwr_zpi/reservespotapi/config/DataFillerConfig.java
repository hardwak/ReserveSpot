package com.pwr_zpi.reservespotapi.config;

import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysis;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysisRepository;
import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.picture.PictureRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatistic;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatisticRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.users.AuthProvider;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataFillerConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PictureRepository pictureRepository;
    private final RestaurantStatisticRepository statisticRepository;
    private final AiAnalysisRepository aiAnalysisRepository;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    public CommandLineRunner dataFiller() {
        return (args) -> {
            userRepository.save(
                    User.builder()
                            .email(adminUsername)
                            .passwordHash(passwordEncoder.encode(adminPassword))
                            .role(Role.ADMIN)
                            .build()
            );
            userRepository.save(
                    User.builder()
                            .email("user")
                            .passwordHash(passwordEncoder.encode("user"))
                            .role(Role.CLIENT)
                            .build()
            );

            User client1 = User.builder()
                    .email("john.doe@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.CLIENT)
                    .name("John Doe")
                    .phoneNumber("+1234567891")
                    .provider(AuthProvider.LOCAL)
                    .build();
            client1 = userRepository.save(client1);

            User client2 = User.builder()
                    .email("jane.smith@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.CLIENT)
                    .name("Jane Smith")
                    .phoneNumber("+1234567892")
                    .provider(AuthProvider.LOCAL)
                    .build();
            client2 = userRepository.save(client2);

            User client3 = User.builder()
                    .email("mike.johnson@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.CLIENT)
                    .name("Mike Johnson")
                    .phoneNumber("+1234567893")
                    .provider(AuthProvider.LOCAL)
                    .build();
            client3 = userRepository.save(client3);

            User owner1 = User.builder()
                    .email("mario.rossi@restaurant.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Mario Rossi")
                    .phoneNumber("+1234567894")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner1 = userRepository.save(owner1);

            User owner2 = User.builder()
                    .email("sakura.tanaka@sushi.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Sakura Tanaka")
                    .phoneNumber("+1234567895")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner2 = userRepository.save(owner2);

            User owner3 = User.builder()
                    .email("chef.bernard@fine.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Chef Bernard")
                    .phoneNumber("+1234567896")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner3 = userRepository.save(owner3);

            Tag italian = tagRepository.save(Tag.builder().name("Italian").build());
            Tag pizza = tagRepository.save(Tag.builder().name("Pizza").build());
            Tag sushi = tagRepository.save(Tag.builder().name("Sushi").build());
            Tag japanese = tagRepository.save(Tag.builder().name("Japanese").build());
            Tag vegetarian = tagRepository.save(Tag.builder().name("Vegetarian").build());
            tagRepository.save(Tag.builder().name("Fast Food").build());
            Tag french = tagRepository.save(Tag.builder().name("French").build());
            tagRepository.save(Tag.builder().name("Vegan").build());
            Tag tagBreakfast = tagRepository.save(Tag.builder().name("Breakfast").build());
            Tag tagSpanish = tagRepository.save(Tag.builder().name("Spanish").build());
            Tag tagGlutenFree = tagRepository.save(Tag.builder().name("Gluten-Free").build());
            Tag tagFamilyFriendly = tagRepository.save(Tag.builder().name("Family-Friendly").build());
            Tag tagPetFriendly = tagRepository.save(Tag.builder().name("Pet-Friendly").build());
            Tag tagCheapEats = tagRepository.save(Tag.builder().name("Cheap Eats").build());
            Tag tagBrunch = tagRepository.save(Tag.builder().name("Brunch").build());
            Tag mexican = tagRepository.save(Tag.builder().name("Mexican").build());
            Tag indian = tagRepository.save(Tag.builder().name("Indian").build());
            Tag steakhouse = tagRepository.save(Tag.builder().name("Steakhouse").build());
            Tag greek = tagRepository.save(Tag.builder().name("Greek").build());
            Tag seafood = tagRepository.save(Tag.builder().name("Seafood").build());
            Tag bar = tagRepository.save(Tag.builder().name("Bar").build());
            Tag fineDining = tagRepository.save(Tag.builder().name("Fine Dining").build());
            Tag romantic = tagRepository.save(Tag.builder().name("Romantic").build());
            Tag outdoorSeating = tagRepository.save(Tag.builder().name("Outdoor Seating").build());

            Picture pic1 = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4")
                    .description("Restaurant interior")
                    .uploadedAt(LocalDateTime.now().minusDays(10))
                    .build());

            Picture pic2 = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
                    .description("Delicious pizza")
                    .uploadedAt(LocalDateTime.now().minusDays(8))
                    .build());

            Picture pic3 = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1579584425555-c3ce17fd4351")
                    .description("Sushi platter")
                    .uploadedAt(LocalDateTime.now().minusDays(5))
                    .build());

            Map<String, String> openingHours1 = new HashMap<>();
            openingHours1.put("monday", "11:00-22:00");
            openingHours1.put("tuesday", "11:00-22:00");
            openingHours1.put("wednesday", "11:00-22:00");
            openingHours1.put("thursday", "11:00-22:00");
            openingHours1.put("friday", "11:00-23:00");
            openingHours1.put("saturday", "11:00-23:00");
            openingHours1.put("sunday", "12:00-21:00");

            Restaurant restaurant1 = Restaurant.builder()
                    .owner(owner1)
                    .name("Mario's Italian Bistro")
                    .address("123 Main Street")
                    .city("New York")
                    .description("Authentic Italian cuisine with a modern twist. Family-owned since 1985.")
                    .openingHours(openingHours1)
                    .averageRating(4.5)
                    .latitude(40.7128)
                    .longitude(-74.0060)
                    .pic("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4")
                    .tags(new HashSet<>(Arrays.asList(italian, pizza)))
                    .pictures(new HashSet<>(Arrays.asList(pic1, pic2)))
                    .build();
            restaurant1 = restaurantRepository.save(restaurant1);

            Map<String, String> openingHours2 = new HashMap<>();
            openingHours2.put("monday", "17:00-23:00");
            openingHours2.put("tuesday", "17:00-23:00");
            openingHours2.put("wednesday", "17:00-23:00");
            openingHours2.put("thursday", "17:00-23:00");
            openingHours2.put("friday", "17:00-00:00");
            openingHours2.put("saturday", "17:00-00:00");
            openingHours2.put("sunday", "17:00-22:00");

            Restaurant restaurant2 = Restaurant.builder()
                    .owner(owner2)
                    .name("Sakura Sushi Bar")
                    .address("456 Ocean Avenue")
                    .city("Los Angeles")
                    .description("Fresh sushi and traditional Japanese dishes. Master chef with 20 years of experience.")
                    .openingHours(openingHours2)
                    .averageRating(4.8)
                    .latitude(34.0522)
                    .longitude(-118.2437)
                    .pic("https://images.unsplash.com/photo-1579584425555-c3ce17fd4351")
                    .tags(new HashSet<>(Arrays.asList(sushi, japanese, seafood)))
                    .pictures(new HashSet<>(Arrays.asList(pic3)))
                    .build();
            restaurant2 = restaurantRepository.save(restaurant2);

            Map<String, String> openingHours3 = new HashMap<>();
            openingHours3.put("monday", "18:00-22:00");
            openingHours3.put("tuesday", "18:00-22:00");
            openingHours3.put("wednesday", "18:00-22:00");
            openingHours3.put("thursday", "18:00-22:00");
            openingHours3.put("friday", "18:00-23:00");
            openingHours3.put("saturday", "18:00-23:00");
            openingHours3.put("sunday", "closed");

            Restaurant restaurant3 = Restaurant.builder()
                    .owner(owner3)
                    .name("Le Château Fine Dining")
                    .address("789 Park Avenue")
                    .city("New York")
                    .description("Elegant French cuisine in an intimate setting. Michelin-starred chef.")
                    .openingHours(openingHours3)
                    .averageRating(4.9)
                    .latitude(40.7589)
                    .longitude(-73.9851)
                    .pic("https://images.unsplash.com/photo-1414235077428-338989a2e8c0")
                    .tags(new HashSet<>(Arrays.asList(french, fineDining, vegetarian)))
                    .pictures(new HashSet<>(Arrays.asList(pic1)))
                    .build();
            restaurant3 = restaurantRepository.save(restaurant3);

            RestaurantTable table1_1 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant1)
                    .tableNumber(1)
                    .capacity(2)
                    .locationInRestaurant("Window")
                    .build());

            RestaurantTable table1_2 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant1)
                    .tableNumber(2)
                    .capacity(4)
                    .locationInRestaurant("Main Hall")
                    .build());

            RestaurantTable table1_3 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant1)
                    .tableNumber(3)
                    .capacity(6)
                    .locationInRestaurant("Private Room")
                    .build());

            RestaurantTable table2_1 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant2)
                    .tableNumber(1)
                    .capacity(2)
                    .locationInRestaurant("Sushi Bar")
                    .build());

            RestaurantTable table2_2 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant2)
                    .tableNumber(2)
                    .capacity(4)
                    .locationInRestaurant("Main Area")
                    .build());

            RestaurantTable table3_1 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant3)
                    .tableNumber(1)
                    .capacity(2)
                    .locationInRestaurant("Private Booth")
                    .build());

            RestaurantTable table3_2 = tableRepository.save(RestaurantTable.builder()
                    .restaurant(restaurant3)
                    .tableNumber(2)
                    .capacity(4)
                    .locationInRestaurant("Main Dining")
                    .build());

            LocalDateTime now = LocalDateTime.now();

            reservationRepository.save(Reservation.builder()
                    .user(client1)
                    .table(table1_1)
                    .reservationDatetime(now.minusDays(5).withHour(19).withMinute(0))
                    .durationMinutes(90)
                    .status(ReservationStatus.COMPLETED)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .user(client2)
                    .table(table2_1)
                    .reservationDatetime(now.minusDays(3).withHour(20).withMinute(0))
                    .durationMinutes(120)
                    .status(ReservationStatus.COMPLETED)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .user(client1)
                    .table(table3_1)
                    .reservationDatetime(now.minusDays(7).withHour(19).withMinute(30))
                    .durationMinutes(150)
                    .status(ReservationStatus.COMPLETED)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .user(client1)
                    .table(table1_2)
                    .reservationDatetime(now.plusDays(2).withHour(19).withMinute(0))
                    .durationMinutes(90)
                    .status(ReservationStatus.CONFIRMED)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .user(client2)
                    .table(table2_2)
                    .reservationDatetime(now.plusDays(3).withHour(20).withMinute(0))
                    .durationMinutes(120)
                    .status(ReservationStatus.CONFIRMED)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .user(client3)
                    .table(table3_2)
                    .reservationDatetime(now.plusDays(5).withHour(18).withMinute(30))
                    .durationMinutes(150)
                    .status(ReservationStatus.PENDING)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .user(client2)
                    .table(table1_3)
                    .reservationDatetime(now.plusDays(1).withHour(19).withMinute(0))
                    .durationMinutes(90)
                    .status(ReservationStatus.CANCELLED)
                    .build());

            reviewRepository.save(Review.builder()
                    .user(client1)
                    .restaurant(restaurant1)
                    .rating(5)
                    .comment("Amazing pizza! The best Italian restaurant in town. Highly recommend the margherita pizza.")
                    .createdAt(now.minusDays(4))
                    .build());

            reviewRepository.save(Review.builder()
                    .user(client2)
                    .restaurant(restaurant2)
                    .rating(5)
                    .comment("Fresh sushi and excellent service. The chef's special was incredible!")
                    .createdAt(now.minusDays(2))
                    .build());

            reviewRepository.save(Review.builder()
                    .user(client1)
                    .restaurant(restaurant3)
                    .rating(5)
                    .comment("Perfect fine dining experience. Every dish was a masterpiece. Worth every penny!")
                    .createdAt(now.minusDays(6))
                    .build());

            statisticRepository.save(RestaurantStatistic.builder()
                    .restaurant(restaurant1)
                    .date(LocalDate.now().minusDays(1))
                    .hourOfDay(19)
                    .averageOccupancy(0.85)
                    .build());

            statisticRepository.save(RestaurantStatistic.builder()
                    .restaurant(restaurant1)
                    .date(LocalDate.now().minusDays(1))
                    .hourOfDay(20)
                    .averageOccupancy(0.95)
                    .build());

            statisticRepository.save(RestaurantStatistic.builder()
                    .restaurant(restaurant2)
                    .date(LocalDate.now().minusDays(1))
                    .hourOfDay(20)
                    .averageOccupancy(0.90)
                    .build());

            statisticRepository.save(RestaurantStatistic.builder()
                    .restaurant(restaurant3)
                    .date(LocalDate.now().minusDays(1))
                    .hourOfDay(19)
                    .averageOccupancy(0.75)
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restaurant1)
                    .summaryText("Overall positive sentiment. Customers love the authentic Italian atmosphere and pizza quality. Some mention long wait times during peak hours.")
                    .sentimentScore(0.85)
                    .lastUpdated(now.minusDays(1))
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restaurant2)
                    .summaryText("Excellent reviews highlighting freshness of ingredients and skilled chefs. Very high customer satisfaction.")
                    .sentimentScore(0.92)
                    .lastUpdated(now.minusDays(1))
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restaurant3)
                    .summaryText("Premium dining experience with outstanding food quality. Customers appreciate the attention to detail and elegant ambiance.")
                    .sentimentScore(0.88)
                    .lastUpdated(now.minusDays(1))
                    .build());


            User owner4 = User.builder()
                    .email("carlos.gomez@tacos.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Carlos Gomez")
                    .phoneNumber("+1234567897")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner4 = userRepository.save(owner4);

            User owner5 = User.builder()
                    .email("priya.patel@curry.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Priya Patel")
                    .phoneNumber("+1234567898")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner5 = userRepository.save(owner5);

            User owner6 = User.builder()
                    .email("jack.butcher@steak.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Jack Butcher")
                    .phoneNumber("+1234567899")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner6 = userRepository.save(owner6);

            User owner7 = User.builder()
                    .email("elena.papadopoulos@greek.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Elena Papadopoulos")
                    .phoneNumber("+1234567800")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner7 = userRepository.save(owner7);

            User owner8 = User.builder()
                    .email("lucas.marin@ocean.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT)
                    .name("Lucas Marin")
                    .phoneNumber("+1234567801")
                    .provider(AuthProvider.LOCAL)
                    .build();
            owner8 = userRepository.save(owner8);


            Picture picMexican = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1565299585323-38d6b0865b47")
                    .description("Tacos and ambiance")
                    .uploadedAt(LocalDateTime.now().minusDays(2))
                    .build());

            Picture picIndian = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1585937421612-70a008356f36")
                    .description("Curry dishes")
                    .uploadedAt(LocalDateTime.now().minusDays(3))
                    .build());

            Picture picSteak = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1544025162-d76694265947")
                    .description("Premium Steaks")
                    .uploadedAt(LocalDateTime.now().minusDays(1))
                    .build());

            Picture picGreek = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1555939594-58d7cb561ad1")
                    .description("Greek Salad and View")
                    .uploadedAt(LocalDateTime.now().minusDays(4))
                    .build());

            Picture picSeafood = pictureRepository.save(Picture.builder()
                    .url("https://images.unsplash.com/photo-1534939561126-855b8675edd7")
                    .description("Fresh Seafood Platter")
                    .uploadedAt(LocalDateTime.now().minusDays(5))
                    .build());


            Map<String, String> standardHours = new HashMap<>();
            standardHours.put("monday", "12:00-22:00");
            standardHours.put("tuesday", "12:00-22:00");
            standardHours.put("wednesday", "12:00-22:00");
            standardHours.put("thursday", "12:00-23:00");
            standardHours.put("friday", "12:00-00:00");
            standardHours.put("saturday", "12:00-00:00");
            standardHours.put("sunday", "12:00-21:00");

            Restaurant restMexican = Restaurant.builder()
                    .owner(owner4)
                    .name("El Camino Cantina")
                    .address("202 Spicy Lane")
                    .city("Austin")
                    .description("Vibrant Mexican street food with a wide selection of tequila and mezcal.")
                    .openingHours(standardHours)
                    .averageRating(4.6)
                    .latitude(30.2672)
                    .longitude(-97.7431)
                    .pic("https://images.unsplash.com/photo-1565299585323-38d6b0865b47")
                    .tags(new HashSet<>(Arrays.asList(mexican, bar, outdoorSeating)))
                    .pictures(new HashSet<>(Arrays.asList(picMexican)))
                    .build();
            restMexican = restaurantRepository.save(restMexican);

            Restaurant restIndian = Restaurant.builder()
                    .owner(owner5)
                    .name("Spice Route Palace")
                    .address("55 Curry Road")
                    .city("London")
                    .description("Authentic North Indian cuisine featuring rich curries and tandoori specials.")
                    .openingHours(standardHours)
                    .averageRating(4.7)
                    .latitude(51.5074)
                    .longitude(-0.1278)
                    .pic("https://images.unsplash.com/photo-1585937421612-70a008356f36")
                    .tags(new HashSet<>(Arrays.asList(indian, vegetarian, fineDining)))
                    .pictures(new HashSet<>(Arrays.asList(picIndian)))
                    .build();
            restIndian = restaurantRepository.save(restIndian);


            Restaurant restSteak = Restaurant.builder()
                    .owner(owner6)
                    .name("The Iron Grill")
                    .address("88 Butcher Street")
                    .city("Chicago")
                    .description("Premium aged steaks grilled to perfection in an industrial-chic setting.")
                    .openingHours(standardHours)
                    .averageRating(4.8)
                    .latitude(41.8781)
                    .longitude(-87.6298)
                    .pic("https://images.unsplash.com/photo-1544025162-d76694265947")
                    .tags(new HashSet<>(Arrays.asList(steakhouse, fineDining, bar)))
                    .pictures(new HashSet<>(Arrays.asList(picSteak)))
                    .build();
            restSteak = restaurantRepository.save(restSteak);


            Restaurant restGreek = Restaurant.builder()
                    .owner(owner7)
                    .name("Santorini Breeze")
                    .address("101 Olive Grove")
                    .city("Miami")
                    .description("Fresh Mediterranean flavors with a focus on seafood and healthy ingredients.")
                    .openingHours(standardHours)
                    .averageRating(4.5)
                    .latitude(25.7617)
                    .longitude(-80.1918)
                    .pic("https://images.unsplash.com/photo-1555939594-58d7cb561ad1")
                    .tags(new HashSet<>(Arrays.asList(greek, seafood, romantic)))
                    .pictures(new HashSet<>(Arrays.asList(picGreek)))
                    .build();
            restGreek = restaurantRepository.save(restGreek);


            Restaurant restSeafood = Restaurant.builder()
                    .owner(owner8)
                    .name("Blue Horizon Seafood")
                    .address("1 Pier Way")
                    .city("San Francisco")
                    .description("Catch of the day served with stunning ocean views.")
                    .openingHours(standardHours)
                    .averageRating(4.4)
                    .latitude(37.7749)
                    .longitude(-122.4194)
                    .pic("https://images.unsplash.com/photo-1534939561126-855b8675edd7")
                    .tags(new HashSet<>(Arrays.asList(seafood, romantic, outdoorSeating)))
                    .pictures(new HashSet<>(Arrays.asList(picSeafood)))
                    .build();
            restSeafood = restaurantRepository.save(restSeafood);



            tableRepository.save(RestaurantTable.builder().restaurant(restMexican).tableNumber(1).capacity(2).locationInRestaurant("Patio").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restMexican).tableNumber(2).capacity(4).locationInRestaurant("Patio").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restMexican).tableNumber(3).capacity(6).locationInRestaurant("Inside").build());

            tableRepository.save(RestaurantTable.builder().restaurant(restIndian).tableNumber(1).capacity(2).locationInRestaurant("Window").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restIndian).tableNumber(2).capacity(4).locationInRestaurant("Main Hall").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restIndian).tableNumber(3).capacity(8).locationInRestaurant("Private Room").build());

            tableRepository.save(RestaurantTable.builder().restaurant(restSteak).tableNumber(1).capacity(2).locationInRestaurant("Bar").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restSteak).tableNumber(2).capacity(4).locationInRestaurant("Booth").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restSteak).tableNumber(3).capacity(6).locationInRestaurant("Main Dining").build());

            tableRepository.save(RestaurantTable.builder().restaurant(restGreek).tableNumber(1).capacity(2).locationInRestaurant("Terrace").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restGreek).tableNumber(2).capacity(4).locationInRestaurant("Terrace").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restGreek).tableNumber(3).capacity(4).locationInRestaurant("Inside").build());

            tableRepository.save(RestaurantTable.builder().restaurant(restSeafood).tableNumber(1).capacity(2).locationInRestaurant("Ocean View").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restSeafood).tableNumber(2).capacity(4).locationInRestaurant("Ocean View").build());
            tableRepository.save(RestaurantTable.builder().restaurant(restSeafood).tableNumber(3).capacity(6).locationInRestaurant("Deck").build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restMexican)
                    .summaryText("Lively atmosphere, tacos are highly praised. Margaritas are a hit.")
                    .sentimentScore(0.88)
                    .lastUpdated(LocalDateTime.now().minusDays(1))
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restIndian)
                    .summaryText("Rich flavors, authentic spices. Service is attentive and professional.")
                    .sentimentScore(0.91)
                    .lastUpdated(LocalDateTime.now().minusDays(1))
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restSteak)
                    .summaryText("Excellent steak quality, perfectly cooked. Pricey but worth it for special occasions.")
                    .sentimentScore(0.93)
                    .lastUpdated(LocalDateTime.now().minusDays(1))
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restGreek)
                    .summaryText("Fresh ingredients, beautiful setting. Great for a romantic dinner.")
                    .sentimentScore(0.89)
                    .lastUpdated(LocalDateTime.now().minusDays(1))
                    .build());

            aiAnalysisRepository.save(AiAnalysis.builder()
                    .restaurant(restSeafood)
                    .summaryText("Seafood is fresh, views are stunning. Can get crowded on weekends.")
                    .sentimentScore(0.86)
                    .lastUpdated(LocalDateTime.now().minusDays(1))
                    .build());
        };
    }
}
