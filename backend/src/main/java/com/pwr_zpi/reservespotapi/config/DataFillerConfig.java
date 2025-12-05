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
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
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
    private final Random random = new Random();

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

            Map<String, String> stdHours = new HashMap<>();
            stdHours.put("monday", "12:00-22:00"); stdHours.put("tuesday", "12:00-22:00");
            stdHours.put("wednesday", "12:00-22:00"); stdHours.put("thursday", "12:00-23:00");
            stdHours.put("friday", "12:00-00:00"); stdHours.put("saturday", "11:00-00:00");
            stdHours.put("sunday", "12:00-21:00");

            Map<String, String> cafeHours = new HashMap<>();
            cafeHours.put("monday", "08:00-20:00"); cafeHours.put("tuesday", "08:00-20:00");
            cafeHours.put("wednesday", "08:00-20:00"); cafeHours.put("thursday", "08:00-20:00");
            cafeHours.put("friday", "08:00-21:00"); cafeHours.put("saturday", "09:00-21:00");
            cafeHours.put("sunday", "09:00-20:00");

            Map<String, Tag> tags = createTags();

            createUsers(tags);
            List<User> owners = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.RESTAURANT)
                    .collect(Collectors.toList());

            List<Picture> pictures = createCommonPictures();

            createRestaurant("Piec na Szewskiej", "Szewska 44, Wrocław", "Słynna pizza neapolitańska prosto z pieca opalanego drewnem.", owners.get(0), stdHours, 51.109, 17.035, Set.of(tags.get("Italian"), tags.get("Pizza"), tags.get("Casual")), getRandomPics(pictures, 2));
            createRestaurant("Iggy Pizza", "Kuźnicza 10, Wrocław", "Nowoczesna pizzeria z klimatem, świetne drinki i neapolitańska pizza.", owners.get(1), stdHours, 51.111, 17.033, Set.of(tags.get("Italian"), tags.get("Pizza"), tags.get("Bar"), tags.get("Date Night")), getRandomPics(pictures, 2));
            createRestaurant("Vivere Italiano", "Ofiar Oświęcimskich 21, Wrocław", "Elegancka włoska restauracja, idealna na romantyczną kolację.", owners.get(2), stdHours, 51.108, 17.030, Set.of(tags.get("Italian"), tags.get("Fine Dining"), tags.get("Romantic"), tags.get("Pasta")), getRandomPics(pictures, 2));
            createRestaurant("Ragu Pracownia Makaronu", "Sienkiewicza 34, Wrocław", "Ręcznie robione makarony, które podbiły serca Wrocławian.", owners.get(3), stdHours, 51.115, 17.045, Set.of(tags.get("Italian"), tags.get("Pasta"), tags.get("Casual")), getRandomPics(pictures, 2));
            createRestaurant("Si", "Bogusławskiego 89, Wrocław", "Włoska kuchnia pod nasypem kolejowym. Klimatycznie i smacznie.", owners.get(4), stdHours, 51.100, 17.030, Set.of(tags.get("Italian"), tags.get("Pizza"), tags.get("Date Night")), getRandomPics(pictures, 2));
            createRestaurant("Classico di Magnolia", "Legnicka 58, Wrocław", "Włoska restauracja w sercu centrum handlowego.", owners.get(0), stdHours, 51.119, 16.989, Set.of(tags.get("Italian"), tags.get("Family Friendly")), getRandomPics(pictures, 1));
            createRestaurant("Tutti Santi", "Hallera 52, Wrocław", "Prawdziwa włoska pizza mistrza Valerio Valle.", owners.get(1), stdHours, 51.089, 17.006, Set.of(tags.get("Italian"), tags.get("Pizza")), getRandomPics(pictures, 1));
            createRestaurant("Oliwa i Ogień", "Igielna 11, Wrocław", "Przytulne miejsce z kuchnią śródziemnomorską w rynku.", owners.get(2), stdHours, 51.112, 17.033, Set.of(tags.get("Italian"), tags.get("Romantic")), getRandomPics(pictures, 1));

            createRestaurant("Konspira", "Plac Solny 11, Wrocław", "Podróż w czasie do PRL-u z solidną porcją polskiego jedzenia.", owners.get(3), stdHours, 51.109, 17.029, Set.of(tags.get("Polish"), tags.get("Casual"), tags.get("Meat")), getRandomPics(pictures, 2));
            createRestaurant("Kurna Chata", "Odrzańska 7, Wrocław", "Tradycyjne polskie smaki w rustykalnym wnętrzu.", owners.get(4), stdHours, 51.112, 17.032, Set.of(tags.get("Polish"), tags.get("Family Friendly"), tags.get("Meat")), getRandomPics(pictures, 2));
            createRestaurant("Piwnica Świdnicka", "Rynek Ratusz 1, Wrocław", "Najstarsza restauracja w Europie, działająca od 1273 roku.", owners.get(0), stdHours, 51.109, 17.031, Set.of(tags.get("Polish"), tags.get("Fine Dining"), tags.get("Bar")), getRandomPics(pictures, 2));
            createRestaurant("Chatka przy Jatkach", "Odrzańska 7, Wrocław", "Domowe obiady i klimat starówki.", owners.get(1), stdHours, 51.111, 17.031, Set.of(tags.get("Polish"), tags.get("Casual")), getRandomPics(pictures, 1));
            createRestaurant("Młoda Polska", "Plac Solny 4, Wrocław", "Nowoczesna kuchnia polska w wykonaniu Beaty Śniechowskiej.", owners.get(2), stdHours, 51.109, 17.028, Set.of(tags.get("Polish"), tags.get("Fine Dining"), tags.get("Modern")), getRandomPics(pictures, 2));
            createRestaurant("Browar Złoty Pies", "Rynek 41, Wrocław", "Rzemieślnicze piwo i polska kuchnia w samym rynku.", owners.get(3), stdHours, 51.110, 17.033, Set.of(tags.get("Polish"), tags.get("Bar"), tags.get("Craft Beer")), getRandomPics(pictures, 1));
            createRestaurant("Pierogarnia Stary Młyn", "Rynek 26, Wrocław", "Najlepsze pieczone pierogi w mieście.", owners.get(4), stdHours, 51.110, 17.030, Set.of(tags.get("Polish"), tags.get("Family Friendly")), getRandomPics(pictures, 1));

            createRestaurant("Panda Ramen", "Białoskórnicza 17, Wrocław", "Kultowy ramen we Wrocławiu. Długie kolejki, ale warto.", owners.get(0), stdHours, 51.110, 17.028, Set.of(tags.get("Asian"), tags.get("Japanese"), tags.get("Ramen")), getRandomPics(pictures, 2));
            createRestaurant("Osiem Misek", "Włodkowica 27, Wrocław", "Bułeczki bao i autorskie dania azjatyckie.", owners.get(1), stdHours, 51.108, 17.026, Set.of(tags.get("Asian"), tags.get("Casual"), tags.get("Street Food")), getRandomPics(pictures, 2));
            createRestaurant("Sushi Corner", "Włodkowica 11, Wrocław", "Wysokiej jakości sushi w dzielnicy czterech wyznań.", owners.get(2), stdHours, 51.109, 17.027, Set.of(tags.get("Asian"), tags.get("Japanese"), tags.get("Sushi")), getRandomPics(pictures, 2));
            createRestaurant("Woo Thai", "Grunwaldzka 67, Wrocław", "Autentyczna kuchnia tajska, ostro i smacznie.", owners.get(3), stdHours, 51.114, 17.063, Set.of(tags.get("Asian"), tags.get("Thai"), tags.get("Spicy")), getRandomPics(pictures, 1));
            createRestaurant("Dim Sum Garden", "Podwale 83, Wrocław", "Chińskie pierożki na parze i nie tylko.", owners.get(4), stdHours, 51.105, 17.038, Set.of(tags.get("Asian"), tags.get("Chinese"), tags.get("Casual")), getRandomPics(pictures, 1));
            createRestaurant("Chingu Korean BBQ", "Biskupia 4, Wrocław", "Koreański grill i bibimbap.", owners.get(0), stdHours, 51.106, 17.035, Set.of(tags.get("Asian"), tags.get("Korean"), tags.get("Meat")), getRandomPics(pictures, 1));
            createRestaurant("77 Sushi", "Szewska 77, Wrocław", "Popularna sieciówka z dobrym sushi.", owners.get(1), stdHours, 51.113, 17.036, Set.of(tags.get("Asian"), tags.get("Sushi")), getRandomPics(pictures, 1));
            createRestaurant("Thali", "Curie-Skłodowskiej 5, Wrocław", "Prawdziwe indyjskie curry w dobrej cenie.", owners.get(2), stdHours, 51.109, 17.055, Set.of(tags.get("Indian"), tags.get("Spicy"), tags.get("Vegetarian")), getRandomPics(pictures, 1));
            createRestaurant("Masala Grill & Bar", "Kuźnicza 3, Wrocław", "Nowoczesna kuchnia indyjska w centrum.", owners.get(3), stdHours, 51.112, 17.032, Set.of(tags.get("Indian"), tags.get("Fine Dining")), getRandomPics(pictures, 1));
            createRestaurant("Mango Mama", "Jedności Narodowej 77, Wrocław", "Azjatycki fusion, świetne curry i drinki.", owners.get(4), stdHours, 51.122, 17.044, Set.of(tags.get("Asian"), tags.get("Indian"), tags.get("Thai")), getRandomPics(pictures, 1));

            createRestaurant("Pasibus", "Świdnicka 11, Wrocław", "Wrocławski klasyk burgerowy. Od foodtrucka do legendy.", owners.get(0), stdHours, 51.106, 17.031, Set.of(tags.get("Burger"), tags.get("American"), tags.get("Street Food")), getRandomPics(pictures, 2));
            createRestaurant("Soczewka", "Rynek 21, Wrocław", "Gourmet burgery w samym rynku.", owners.get(1), stdHours, 51.110, 17.031, Set.of(tags.get("Burger"), tags.get("American"), tags.get("Casual")), getRandomPics(pictures, 1));
            createRestaurant("Moaburger", "Plac Solny 10, Wrocław", "Wielkie nowozelandzkie burgery.", owners.get(2), stdHours, 51.109, 17.029, Set.of(tags.get("Burger"), tags.get("American"), tags.get("Meat")), getRandomPics(pictures, 1));
            createRestaurant("Whiskey in the Jar", "Rynek 23, Wrocław", "Steki, burgery, motocykle i rock'n'roll.", owners.get(3), stdHours, 51.110, 17.030, Set.of(tags.get("American"), tags.get("Steakhouse"), tags.get("Bar"), tags.get("Music")), getRandomPics(pictures, 1));
            createRestaurant("Panczo", "Św. Antoniego 35, Wrocław", "Tex-mex w najlepszym wydaniu. Tacos i tequila.", owners.get(4), stdHours, 51.109, 17.023, Set.of(tags.get("Mexican"), tags.get("Spicy"), tags.get("Bar")), getRandomPics(pictures, 2));
            createRestaurant("El Gordito", "Armii Krajowej 14, Wrocław", "Autentyczne meksykańskie tacos.", owners.get(0), stdHours, 51.085, 17.045, Set.of(tags.get("Mexican"), tags.get("Street Food")), getRandomPics(pictures, 1));
            createRestaurant("Campo Modern Grill", "Podwale 83, Wrocław", "Najlepsze steki w Polsce (wyróżnione w rankingu).", owners.get(1), stdHours, 51.105, 17.038, Set.of(tags.get("Steakhouse"), tags.get("Fine Dining"), tags.get("Meat")), getRandomPics(pictures, 2));
            createRestaurant("Road American Restaurant", "Sky Tower, Wrocław", "Amerykańska kuchnia z widokiem.", owners.get(2), stdHours, 51.094, 17.020, Set.of(tags.get("American"), tags.get("Burger")), getRandomPics(pictures, 1));

            createRestaurant("Vega", "Rynek 27, Wrocław", "Najstarsza wegańska restauracja w Polsce.", owners.get(3), stdHours, 51.110, 17.032, Set.of(tags.get("Vegan"), tags.get("Vegetarian"), tags.get("Healthy")), getRandomPics(pictures, 2));
            createRestaurant("Krowarzywa", "Rzeźnicza 34, Wrocław", "Wegańskie burgery, które smakują każdemu.", owners.get(4), stdHours, 51.111, 17.029, Set.of(tags.get("Vegan"), tags.get("Burger"), tags.get("Fast Food")), getRandomPics(pictures, 1));
            createRestaurant("Wilk Syty", "Trzebnicka 3, Wrocław", "Autorska kuchnia roślinna na Nadodrzu.", owners.get(0), stdHours, 51.121, 17.033, Set.of(tags.get("Vegan"), tags.get("Casual"), tags.get("Healthy")), getRandomPics(pictures, 1));
            createRestaurant("Tajfun", "Cybulskiego 3, Wrocław", "Kuchnia azjatycka w wersji 100% roślinnej.", owners.get(1), stdHours, 51.115, 17.025, Set.of(tags.get("Vegan"), tags.get("Asian")), getRandomPics(pictures, 1));

            createRestaurant("Gniazdo", "Świdnicka 36, Wrocław", "Kawa specialty i świetne śniadania.", owners.get(2), cafeHours, 51.104, 17.032, Set.of(tags.get("Cafe"), tags.get("Breakfast"), tags.get("Coffee")), getRandomPics(pictures, 2));
            createRestaurant("Charlotte", "Św. Antoniego 2, Wrocław", "Francuskie pieczywo, wino i śniadania cały dzień.", owners.get(3), cafeHours, 51.109, 17.024, Set.of(tags.get("Cafe"), tags.get("Breakfast"), tags.get("French")), getRandomPics(pictures, 2));
            createRestaurant("Vincent", "Ruska 39, Wrocław", "Boulangerie patisserie - francuskie wypieki.", owners.get(4), cafeHours, 51.110, 17.025, Set.of(tags.get("Cafe"), tags.get("Dessert"), tags.get("French")), getRandomPics(pictures, 1));
            createRestaurant("Dinette", "Plac Teatralny 8, Wrocław", "Kultowe miejsce na śniadanie we Wrocławiu.", owners.get(0), cafeHours, 51.105, 17.033, Set.of(tags.get("Breakfast"), tags.get("Brunch"), tags.get("Modern")), getRandomPics(pictures, 1));
            createRestaurant("Giszer", "Szewska 27, Wrocław", "Kawa i bajgle.", owners.get(1), cafeHours, 51.112, 17.036, Set.of(tags.get("Cafe"), tags.get("Breakfast")), getRandomPics(pictures, 1));
            createRestaurant("Monopol", "Heleny Modrzejewskiej 2, Wrocław", "Luksusowa restauracja w historycznym hotelu.", owners.get(2), stdHours, 51.105, 17.033, Set.of(tags.get("Fine Dining"), tags.get("Hotel"), tags.get("European")), getRandomPics(pictures, 2));
            createRestaurant("La Maddalena", "Pomorska 1, Wrocław", "Restauracja na wodzie z widokiem na uniwersytet.", owners.get(3), stdHours, 51.115, 17.032, Set.of(tags.get("Fine Dining"), tags.get("Seafood"), tags.get("Romantic")), getRandomPics(pictures, 2));
            createRestaurant("Bernard", "Rynek 35, Wrocław", "Kuchnia czeska i europejska w sercu rynku.", owners.get(4), stdHours, 51.110, 17.031, Set.of(tags.get("European"), tags.get("Meat"), tags.get("Beer")), getRandomPics(pictures, 1));
            createRestaurant("Hurry Curry", "Szewska 22, Wrocław", "Szybkie i smaczne curry w różnych stylach.", owners.get(0), stdHours, 51.111, 17.036, Set.of(tags.get("Indian"), tags.get("Thai"), tags.get("Casual")), getRandomPics(pictures, 1));
            createRestaurant("Szajnochy 11", "Szajnochy 11, Wrocław", "Autorskie sushi w eleganckim wydaniu.", owners.get(1), stdHours, 51.109, 17.028, Set.of(tags.get("Sushi"), tags.get("Fine Dining"), tags.get("Date Night")), getRandomPics(pictures, 2));


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
                    .status(ReservationStatus.CONFIRMED)
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

            if (client1.getFavoriteRestaurants() == null) {
                client1.setFavoriteRestaurants(new HashSet<>());
            }
            client1.getFavoriteRestaurants().add(restaurant1);
            userRepository.save(client1);

            System.out.println("Data filled");
        };
    }

    private void createRestaurant(String name, String address, String desc, User owner, Map<String, String> hours, double lat, double lon, Set<Tag> tags, Set<Picture> pics) {
        Restaurant r = Restaurant.builder()
                .name(name)
                .address(address)
                .city("Wrocław")
                .description(desc)
                .owner(owner)
                .openingHours(hours)
                .latitude(lat)
                .longitude(lon)
                .averageRating(3.5 + (random.nextDouble() * 1.5))
                .tags(tags)
                .pictures(pics)
                .pic(pics.stream().findFirst().map(Picture::getUrl).orElse(null))
                .build();

        r = restaurantRepository.save(r);

        int tablesCount = 5 + random.nextInt(10);
        for (int i = 1; i <= tablesCount; i++) {
            int capacity = (i % 3 == 0) ? 6 : (i % 2 == 0 ? 4 : 2);
            tableRepository.save(RestaurantTable.builder()
                    .restaurant(r)
                    .tableNumber(i)
                    .capacity(capacity)
                    .locationInRestaurant(i < 5 ? "Window" : "Main Hall")
                    .build());
        }

        aiAnalysisRepository.save(AiAnalysis.builder()
                .restaurant(r)
                .summaryText("AI Summary for " + name + ": Customers generally appreciate the atmosphere and food quality. Popular spot for " + tags.stream().findFirst().get().getName() + " cuisine.")
                .sentimentScore(0.7 + (random.nextDouble() * 0.25))
                .lastUpdated(LocalDateTime.now())
                .build());

        statisticRepository.save(RestaurantStatistic.builder()
                .restaurant(r)
                .date(LocalDate.now().minusDays(1))
                .hourOfDay(19)
                .averageOccupancy(0.4 + (random.nextDouble() * 0.5))
                .build());
    }

    private Map<String, Tag> createTags() {
        String[] tagNames = {
                "Italian", "Polish", "Indian", "Mexican", "Japanese", "Sushi", "Burger", "Pizza",
                "Vegan", "Vegetarian", "Asian", "Thai", "French", "American", "Seafood", "Steakhouse",
                "Cafe", "Breakfast", "Dessert", "Bar", "Pub", "Craft Beer", "Fine Dining", "Casual",
                "Family Friendly", "Date Night", "Street Food", "Healthy", "Modern", "Meat", "Pasta",
                "Chinese", "Korean", "European", "Hotel", "Beer", "Ramen", "Coffee", "Brunch", "Romantic",
                "Asian", "Spicy", "Music", "Fast Food"
        };

        Map<String, Tag> tagMap = new HashMap<>();
        for (String name : tagNames) {
            Tag t = tagRepository.findByNameIgnoreCase(name).orElseGet(() ->
                    tagRepository.save(Tag.builder().name(name).build())
            );
            tagMap.put(name, t);
        }
        return tagMap;
    }

    private void createUsers(Map<String, Tag> tags) {
        if (userRepository.findByEmail(adminUsername).isEmpty()) {
            userRepository.save(User.builder().email(adminUsername).name("Super Admin").passwordHash(passwordEncoder.encode(adminPassword)).role(Role.ADMIN).provider(AuthProvider.LOCAL).build());
        }
        if (userRepository.findByEmail("user").isEmpty()) {
            userRepository.save(User.builder().email("user").name("Test User").passwordHash(passwordEncoder.encode("user")).role(Role.CLIENT).provider(AuthProvider.LOCAL).build());
        }
        for (int i = 1; i <= 5; i++) {
            String email = "owner" + i + "@test.com";
            if (userRepository.findByEmail(email).isEmpty()) {
                userRepository.save(User.builder()
                        .email(email)
                        .name("Owner " + i)
                        .passwordHash(passwordEncoder.encode("password"))
                        .role(Role.RESTAURANT)
                        .provider(AuthProvider.LOCAL)
                        .build());
            }
        }
    }

    private List<Picture> createCommonPictures() {
        List<Picture> pics = new ArrayList<>();
        pics.add(pictureRepository.save(Picture.builder().url("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/28/71/47/c2/seafood-buffet-at-mezz.jpg?w=900&h=500&s=1").description("Cozy Interior").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://popmenucloud.com/jwutdrmk/67f010e1-4698-4340-9ba4-a1c9428699f1.jpg").description("Food Plate").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://cdn.prod.website-files.com/60414b21f1ffcdbb0d5ad688/65206c769c62b75fd6fd0c67_alex-haney-CAhjZmVk5H4-unsplash.jpg").description("Ingredients").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://eu-images.contentstack.com/v3/assets/blt7b94604c32d7613d/bltc10810c25f1aa21e/692dd14d108922509a6b84a0/1._Pauline_copy.jpg?width=1280&auto=webp&quality=80&disable=upscale").description("Bar").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://themillontheriver.com/wp-content/uploads/2025/01/54244983102_1bb2fc8a89_k-ezgif.com-crop.jpg").description("Patio").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://www.gordonramsayrestaurants.com/assets/Uploads/_resampled/CroppedFocusedImage121578650-50-GRR-LC-22B-NOVEMBER-2025-NEW-YEARS-EVE-LATEEF288-lf8t7x.jpg").description("Special Dish").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://media.istockphoto.com/id/1054319798/photo/group-of-happy-friends-having-breakfast-in-the-restaurant.jpg?s=612x612&w=0&k=20&c=rdb2gaIzr5n2eZthvK1B73LQa3yapubVD2AM_-SF50o=").description("Modern Look").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://i.insider.com/5dc498fc695b58645d6f1dab?width=1200&format=jpeg").description("Dessert").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://images.rawpixel.com/image_social_landscape/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDIyLTA1L2EwMDkta2Fib29tcGljcy0wMDEuanBn.jpg").description("Salad").uploadedAt(LocalDateTime.now()).build()));
        pics.add(pictureRepository.save(Picture.builder().url("https://static01.nyt.com/images/2024/10/16/multimedia/16best-restaurants-nashville15-jbkq/07best-restaurants-nashville15-jbkq-videoSixteenByNineJumbo1600.jpg").description("Evening").uploadedAt(LocalDateTime.now()).build()));
        return pics;
    }

    private Set<Picture> getRandomPics(List<Picture> allPics, int count) {
        Set<Picture> selected = new HashSet<>();
        for (int i = 0; i < count; i++) {
            selected.add(allPics.get(random.nextInt(allPics.size())));
        }
        return selected;
    }

}
