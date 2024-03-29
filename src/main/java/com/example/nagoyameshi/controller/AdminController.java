package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.NagoyameshiuserRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.service.NagoyameshiuserService;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.StripeService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final RestaurantService restaurantService;
	private final NagoyameshiuserService userService;
	private final CategoryRepository categoryRepository;
	private final NagoyameshiuserRepository nagoyameshiuserRepository;
	private final ReservationRepository reservationRepository;
	private final StripeService stripeService;
	private final RestaurantRepository restaurantRepository;

	public AdminController(RestaurantService restaurantService, NagoyameshiuserService userService,
			CategoryRepository categoryRepository, NagoyameshiuserRepository nagoyameshiuserRepository,
			ReservationRepository reservationRepository, StripeService stripeService,
			RestaurantRepository restaurantRepository) {
		this.restaurantService = restaurantService;
		this.userService = userService;
		this.categoryRepository = categoryRepository;
		this.nagoyameshiuserRepository = nagoyameshiuserRepository;
		this.reservationRepository = reservationRepository;
		this.stripeService = stripeService;
		this.restaurantRepository = restaurantRepository;
	}

//	@GetMapping("/admin/index")
	
	@GetMapping
	public String dashboard(Model model) {
		List<Restaurant> restaurants = restaurantRepository.findAll(); // この行を追加
		List<Restaurant> newRestaurants = restaurantService.findNewRestaurants(5); // 例えば最新の5件
		List<Category> allCategories = categoryRepository.findAll();
		List<Category> topCategories = allCategories.stream().limit(5).collect(Collectors.toList());
		List<Category> otherCategories = allCategories.stream().skip(5).collect(Collectors.toList());
		List<Restaurant> sortedRestaurants = restaurantService.sortByRatingDesc(restaurants);
		List<Restaurant> highlyRatedRestaurants = sortedRestaurants.subList(0, Math.min(sortedRestaurants.size(), 5));
		model.addAttribute("highlyRatedRestaurants", highlyRatedRestaurants);
		long totalFreeMembers = nagoyameshiuserRepository.countByRole_Name("ROLE_FREE_MEMBER");
		long totalPaidMembers = nagoyameshiuserRepository.countByRole_Name("ROLE_PAID_MEMBER");
		long totalReservations = reservationRepository.count();

		LocalDate now = LocalDate.now();
		LocalDate startOfMonth = now.withDayOfMonth(1);
		long salesForThisMonth;

		try {
			salesForThisMonth = stripeService.getMonthlyTotalRevenue(startOfMonth);
		} catch (Exception e) {
			salesForThisMonth = 0;
		}

		model.addAttribute("topCategories", topCategories);
		model.addAttribute("otherCategories", otherCategories);
		model.addAttribute("newRestaurants", newRestaurants);
		model.addAttribute("totalRestaurants", restaurantService.getTotalNumberOfRestaurants());
		model.addAttribute("totalUsers", userService.getTotalNumberOfUsers());
		model.addAttribute("totalFreeMembers", totalFreeMembers);
		model.addAttribute("totalPaidMembers", totalPaidMembers);
		model.addAttribute("totalReservations", totalReservations);
		model.addAttribute("salesForThisMonth", salesForThisMonth);

		return "admin/index";
	}

}