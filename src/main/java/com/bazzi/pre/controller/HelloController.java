package com.bazzi.pre.controller;

import com.bazzi.core.annotation.AllowAccess;
import com.bazzi.core.generic.Result;
import com.bazzi.pre.entity.LoginBean;
import com.bazzi.pre.model.User;
import com.bazzi.pre.service.AsyncService;
import com.bazzi.pre.service.RedisService;
import com.bazzi.pre.service.UserService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class HelloController {

	@Resource
	private RedisService redisService;
	@Resource
	private UserService userService;
	@Resource
	private AsyncService asyncService;

	@GetMapping(value = "lock", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<Boolean> lock() {
		Boolean flag = redisService.lock("orderCRM", "orderCRM_web", 100000);
		return Result.success(flag);
	}

	@GetMapping(value = "unlock", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<Boolean> unlock() {
		Boolean flag = redisService.releaseLock("orderCRM", "orderCRM_web");
		return Result.success(flag);
	}

	@AllowAccess
	@GetMapping(value = "idx", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<User> idx(@RequestParam int age) {
		User user = userService.findUserById(1L);
		redisService.set("age", age);
		redisService.set("user", user);
		log.info(redisService.get("user").toString());
		return Result.success(user);
	}

	@AllowAccess
	@GetMapping(value = "page", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<String> page(HttpSession session) throws JsonProcessingException {

		List<User> list = userService.findUserPage(1, 5);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
		redisService.set("users", list);
		asyncService.sleepAndPrint(list.get(0));
//		session.setAttribute("redis","spring-session");
		log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		return Result.success(objectMapper.writeValueAsString(list));
	}

//	@AllowAccess
	@GetMapping(value = "p", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<PageInfo<User>> p() {
		return Result.success(userService.findPage(2, 3));
	}

	@AllowAccess
	@PostMapping(value = "checkParam", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<String> checkParam(@RequestBody LoginBean loginBean) {
		return Result.success("Test is ok");
	}

	@AllowAccess
	@GetMapping(value = "checkP", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<String> checkP(@NotBlank String userName,
								 @NotBlank @Length(min = 6, max = 64) String password,
								 @Min(value = 13) Integer age, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		return Result.success("Test is checkP");
	}

}
