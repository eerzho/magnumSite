package kz.iitu.javaee.ilyasProject.controllers;

import kz.iitu.javaee.ilyasProject.entities.*;
import kz.iitu.javaee.ilyasProject.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class MainController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private CompaniesRepository companiesRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping(path = "/")
    public String index(Model model){
        model.addAttribute("classActiveSettingsIndexPage", "active");
        return "index";
    }
    public Users getUserData(){
        Users userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            User secUser = (User)authentication.getPrincipal();

            System.out.println("TUTA GET USERNAME" + secUser.getUsername());
            userData = userRepository.findByEmail(secUser.getUsername());
        }
        return userData;
    }
    public Products getProductData(){
        Products productData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            Products secProducts = (Products) authentication.getPrincipal();

            System.out.println("TUTA GET PRODUCTNAME" + secProducts.getName());
            productData = productRepository.findByName(secProducts.getName());
        }
        return productData;
    }
//    эти функций для гостя сайта
    @GetMapping(path = "/login")
    public String login(Model model){
        return "guest/login";
    }


    @GetMapping(path = "/registration")
    public String registration (Model model){
        return "guest/registration";
    }

    @PostMapping(value = "/register")
    public String register(
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password,
            @RequestParam(name = "user_rePassword") String rePassword,
            Model model){
        model.addAttribute("save_name", name);
        model.addAttribute("save_email", email);
        if (userRepository.findByEmail(email) == null) {
            if (password.length() > 6) {
                if (password.equals(rePassword)) {
                    Set<Roles> roles = new HashSet<>();
                    Roles r = rolesRepository.findById(2L).orElse(null);
                    roles.add(r);
                    Users user;
                    user = new Users(email, passwordEncoder.encode(password), name, true, roles);

                    userRepository.save(user);
                }
                else {
                    model.addAttribute("error", "Password mismatch");
                    return "guest/registration";
                }
            }
            else {
                model.addAttribute("error", "Password must be at least 6");
                return "guest/registration";
            }
        }
        else{
            model.addAttribute("error", "This email address is already registered");
            return "guest/registration";
        }
        return "guest/login";
    }
//    функций для пользавателя
    @GetMapping(path = "/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model){
        model.addAttribute("user", getUserData());
        return "user/profile";
    }

    @GetMapping(path = "/updatePassword/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updatePasswordPage(ModelMap model, @PathVariable(name = "id") Long id){
        Users user = userRepository.findById(id).orElse(null);
        model.addAttribute("user",user);
        return "user/updatePasswordPage";
    }

    @PostMapping(value = "/changePassword")
    @PreAuthorize("isAuthenticated()")
    public String changePassword(
            @RequestParam(name = "oldPassword") String oldPassword,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "rePassword") String rePassword,
            Model model){
        Users user = userRepository.findById(getUserData().getId()).orElse(null);
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            if (password.length() > 6) {
                if (password.equals(rePassword)) {
                    user.setPassword(passwordEncoder.encode(password));
                }
                else {
                    model.addAttribute("error", "Password mismatch");
                    return "user/updatePasswordPage";
                }
            }
            else {
                model.addAttribute("error", "Password must be at least 6");
                return "user/updatePasswordPage";
            }
        }
        else {
            model.addAttribute("error", "Old password is entered incorrectly");
            return "user/updatePasswordPage";
        }
        userRepository.save(user);
        return "redirect:/profile";
    }

    @GetMapping(path = "/updateData/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updateDataPage(ModelMap model, @PathVariable(name = "id") Long id){
        Users user = userRepository.findById(id).orElse(null);
        model.addAttribute("user",user);
        return "user/updateDataPage";
    }

    @PostMapping(value = "/changeData")
    @PreAuthorize("isAuthenticated()")
    public String changeData(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "password") String password,
            Model model){
        Users user = userRepository.findById(getUserData().getId()).orElse(null);
        if (passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setFullName(name);
            userRepository.save(user);
        }
        else {
            model.addAttribute("error", "Password is entered incorrectly");
            return "user/updateDataPage";
        }
        return "redirect:/profile";
    }

    @GetMapping(path = "/catalog")
    public String catalog (Model model){
        List<Products> allProducts = productRepository.findAll();
        model.addAttribute("productList", allProducts);
        return "catalog";
    }

    @GetMapping(path = "/productDetails/{id}")
    public String productDetails(ModelMap model, @PathVariable(name = "id") Long id){
        Products products = productRepository.findById(id).orElse(null);
        assert products != null;
        List<Companies> companies = new ArrayList<>(products.getCompany());
        List<Categories> categories = new ArrayList<>(products.getCategory());
        model.addAttribute("companies", companies);
        model.addAttribute("categories", categories);
        model.addAttribute("product", products);
        return "productDetails";
    }

    @GetMapping(value = "/addCart")
    @PreAuthorize("isAuthenticated()")
    public String addCart(
            @RequestParam(name = "productId") Long productId,
            ModelMap model){
        Cart cart = new Cart();
        Products products = productRepository.findById(productId).orElse(null);
        List<Cart> allCarts = cartRepository.findAll();
        boolean test = false;
        for (Cart u: allCarts) {
            if (u.getProduct() == products) {
                test = true;
                break;
            }
        }
        if (!test) {
            cart.setUser(getUserData());
            cart.setProduct(products);
            cartRepository.save(cart);
        }
        else {
            return "redirect:/productDetails/" + productId;
        }
        return "redirect:/catalog";
    }

    @GetMapping(value = "/cart")
    @PreAuthorize("isAuthenticated()")
    public String cart(
            ModelMap model) {
        List<Cart> allCarts = cartRepository.findAll();
        List<Products> productsFromCarts = new ArrayList<>();
        for (Cart u: allCarts) {
            if (u.getUser() == getUserData()) {
                productsFromCarts.add(u.getProduct());
            }
        }
        model.addAttribute("carts", productsFromCarts);
        return "user/cart";
    }
    @GetMapping(value = "/deleteProduct")
    @PreAuthorize("isAuthenticated()")
    public String deleteProduct(
            @RequestParam(name = "productId") Long productId){
        Products products = productRepository.findById(productId).orElse(null);
        Users user = getUserData();
        List<Cart> allCarts = cartRepository.findAll();
        Long id = null;
        for (Cart u: allCarts){
            if (u.getProduct() == products && u.getUser() == user) {
                id = u.getId();
            }
        }
        cartRepository.deleteById(id);
        return "redirect:/cart";
    }
    //    функций для admina
    @GetMapping(path = "/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String users(ModelMap model){
        model.addAttribute("user", getUserData());

        List<Users> allUsers = userRepository.findAll();
        List<Users> simpleUsers = new ArrayList<>();

        Roles r = rolesRepository.findById(2L).orElse(null);
        System.out.println(r+"ASDASDASDASDASDAS");
        for(Users u:allUsers){
            if(u.getRoles().contains(r))
                simpleUsers.add(u);
        }
        model.addAttribute("userList", simpleUsers);
        return "admin/users";
    }
    @PostMapping(value = "/addUser")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addUser(
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password,
            Model model){
        if (userRepository.findByEmail(email) == null) {
            if (password.length() > 6) {
                Set<Roles> roles = new HashSet<>();
                Roles r = rolesRepository.findById(2L).orElse(null);
                roles.add(r);
                Users user;
                user = new Users(email, passwordEncoder.encode(password), name, true, roles);

                userRepository.save(user);
            }
        }
        return "redirect:/users";
    }

    @GetMapping(path = "/editUser/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String userEdit(ModelMap model, @PathVariable(name = "id") Long id){
        Users user = userRepository.findById(id).orElse(null);
        model.addAttribute("user",user);
        return "admin/editUser";
    }

    @PostMapping(value = "/updateUser")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String updateUser(
            @RequestParam(name = "user_id") Long id,
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password,
            Model model){

        Users user = userRepository.findById(id).orElse(null);

        user.setEmail(email);
        user.setFullName(name);
        if (password.length() > 6) {
            if (!password.equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(password));

            }
        }
        else {
            model.addAttribute("error", "Password must be at least 6");
        }
        model.addAttribute("succeed", "Succeed");
        userRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping(value = "/block")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String block(
            @RequestParam(name = "user_id") Long id){
        Users user = userRepository.findById(id).orElse(null);
        boolean status;
        assert user != null;
        status = !user.getIsActive();
        user.setIsActive(status);
        userRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping(value = "/delete")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String delete(
            @RequestParam(name = "user_id") Long id){
        Users user = userRepository.findById(id).orElse(null);
        userRepository.deleteById(id);
        return "redirect:/users";
    }

    @GetMapping(path = "/addProduct")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addProduct(Model model){
        List<Categories> allCategories = categoriesRepository.findAll();
        List<Categories> simpleCategories = new ArrayList<>();
        for(Categories u:allCategories){
            simpleCategories.add(u);
        }
        model.addAttribute("categoriesList", simpleCategories);
        List<Companies> allCompanies = companiesRepository.findAll();
        List<Companies> simpleCompanies = new ArrayList<>();
        for(Companies u:allCompanies){
            simpleCompanies.add(u);
        }
        model.addAttribute("companiesList", simpleCompanies);
        return "admin/addProduct";
    }
    @PostMapping(value = "/addProducts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addProduts(
            @RequestParam(name = "photo") MultipartFile photo,
            @RequestParam(name = "available") Boolean available,
            @RequestParam(name = "product_name") String product_name,
            @RequestParam(name = "price") Integer price,
            @RequestParam(name = "category") Long category,
            @RequestParam(name = "company") Long company,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "new_company") String new_company,
            @RequestParam(name = "new_category") String new_category,
            Model model) throws IOException {
        String resultFileName = " ";
        Set<Companies> companies = new HashSet<>();
        if (company == null) {
            Companies newCompany;
            newCompany = new Companies(new_company);
            companiesRepository.save(newCompany);
            List<Companies> allCompanies = companiesRepository.findAll();
            for(Companies u:allCompanies){
                if(u.getCompany().equals(new_company)) {
                    Companies com = companiesRepository.findById(u.getId()).orElse(null);
                    companies.add(com);
                    break;
                }
            }
        }
        else {
            Companies com = companiesRepository.findById(company).orElse(null);
            companies.add(com);
        }
        Set<Categories> categories = new HashSet<>();
        if (category == null) {
            Categories newCategory;
            newCategory = new Categories(new_category);
            categoriesRepository.save(newCategory);
            List<Categories> allCategories = categoriesRepository.findAll();
            for(Categories u:allCategories){
                if(u.getCategory().equals(new_category)) {
                    Categories cat = categoriesRepository.findById(u.getId()).orElse(null);
                    categories.add(cat);
                    break;
                }
            }
        }
        else {
            Categories cat = categoriesRepository.findById(category).orElse(null);
            categories.add(cat);
        }
        if (photo != null) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            resultFileName = product_name + photo.getOriginalFilename();
            photo.transferTo(new File(uploadPath + "/" +resultFileName));
        }
        Products product;
        product = new Products(product_name, price, description, available, resultFileName, categories, companies);
        productRepository.save(product);
        return "redirect:/addProduct";
    }

}
