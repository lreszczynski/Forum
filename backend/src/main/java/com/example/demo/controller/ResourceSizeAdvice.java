package com.example.demo.controller;

/*
@ControllerAdvice
public class ResourceSizeAdvice implements ResponseBodyAdvice<Page<?>> {
	
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		//Checks if this advice is applicable.
		//In this case it applies to any endpoint which returns a page.
		return Page.class.isAssignableFrom(returnType.getParameterType());
	}
	
	@Override
	public Page<?> beforeBodyWrite(Page<?> page, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
		serverHttpResponse.getHeaders().add("X-Total-Count",String.valueOf(Objects.requireNonNull(page).getTotalElements()));
		return page;
	}
	
}*/
