package com.gaia3d.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gaia3d.domain.CacheManager;
import com.gaia3d.domain.Menu;
import com.gaia3d.domain.Policy;
import com.gaia3d.service.MenuService;
import com.gaia3d.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 메뉴
 * @author jeongdae
 *
 */
@Slf4j
@Controller
@RequestMapping("/config/")
public class MenuController {
	
	@Autowired
	private MenuService menuService;
	
	/**
	 * 메뉴 정책
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "list-menu.do", method = RequestMethod.GET)
	public String menuList(HttpServletRequest request, Model model) {
		return "/config/list-menu";
	}
	
	/**
	 * 메뉴 목록
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-list-menu.do")
	@ResponseBody
	public Map<String, Object> ajaxListMenu(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String menuTree = null;
		List<Menu> menuList = new ArrayList<>();
		menuList.add(getRootMenu());
		try {
			menuList.addAll(menuService.getListMenu(null));
			menuTree = getMenuTree(menuList);
			log.info("@@ menuTree = {} ", menuTree);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		map.put("menuTree", menuTree);
		return map;
	}
	
	/**
	 * 메뉴 추가
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-insert-menu.do")
	@ResponseBody
	public Map<String, Object> ajaxInsertMenu(HttpServletRequest request, Menu menu) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String menuTree = null;
		List<Menu> menuList = new ArrayList<>();
		menuList.add(getRootMenu());
		try {
			log.info("@@ menu = {} ", menu);
			
			String parent = request.getParameter("parent");
			String depth = request.getParameter("depth");
			if(menu.getName() == null || "".equals(menu.getName())
					|| menu.getName_en() == null || "".equals(menu.getName_en())	
					|| parent == null || "".equals(parent)
					|| menu.getUrl() == null || "".equals(menu.getUrl())
					|| menu.getUse_yn() == null || "".equals(menu.getUse_yn())) {
				
				menuList.addAll(menuService.getListMenu(null));
				
				result = "policy.menu.invalid";
				menuTree = getMenuTree(menuList);
				map.put("result", result);
				map.put("menuTree", menuTree);
				return map;
			}
			
			menu.setParent(Long.parseLong(parent));
			menu.setDepth(Integer.parseInt(depth));
			
			Menu childMenu = menuService.getMaxViewOrderChildMenu(menu.getParent());
			if(childMenu == null) {
				menu.setView_order(1);
			} else {
				menu.setView_order(childMenu.getView_order() + 1);
			}
			
			if("\"null\"".equals(menu.getName_en()) || "null".equals(menu.getName_en())) menu.setName_en("");
			if("\"null\"".equals(menu.getImage()) || "null".equals(menu.getImage())) menu.setImage("");
			if("\"null\"".equals(menu.getImage_alt()) || "null".equals(menu.getImage_alt())) menu.setImage_alt("");
			if("\"null\"".equals(menu.getCss_class()) || "null".equals(menu.getCss_class())) menu.setCss_class("");
			if("\"null\"".equals(menu.getDescription()) || "null".equals(menu.getDescription())) menu.setDescription("");
			if("\"null\"".equals(menu.getDisplay_yn()) || "null".equals(menu.getDisplay_yn())) menu.setDisplay_yn("");
			if("\"null\"".equals(menu.getUrl_alias()) || "null".equals(menu.getUrl_alias())) menu.setUrl_alias("");
			
			menuService.insertMenu(menu);
			menuList.addAll(menuService.getListMenu(null));
			
			menuTree = getMenuTree(menuList);
			log.info("@@ menuTree = {} ", menuTree);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		map.put("menuTree", menuTree);
		return map;
	}
	
	/**
	 * 메뉴 수정
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-update-menu.do")
	@ResponseBody
	public Map<String, Object> ajaxUpdateMenu(HttpServletRequest request, Menu menu) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String menuTree = null;
		List<Menu> menuList = new ArrayList<>();
		menuList.add(getRootMenu());
		try {
			log.info("@@ menu = {} ", menu);
			if(menu.getMenu_id() == null || menu.getMenu_id().longValue() == 0l
					|| menu.getName() == null || "".equals(menu.getName())
					|| menu.getName_en() == null || "".equals(menu.getName_en())
					|| menu.getUrl() == null || "".equals(menu.getUrl())
					|| menu.getUse_yn() == null || "".equals(menu.getUse_yn())) {
				
				menuList.addAll(menuService.getListMenu(null));
				result = "policy.menu.invalid";
				menuTree = getMenuTree(menuList);
				map.put("result", result);
				map.put("menuTree", menuTree);
				return map;
			}
			
			// TODO null 이라는 문자가 들어가면 트리가 표시되지 않음. 나중에 잡자.
			if("\"null\"".equals(menu.getName_en()) || "null".equals(menu.getName_en())) menu.setName_en("");
			if("\"null\"".equals(menu.getImage()) || "null".equals(menu.getImage())) menu.setImage("");
			if("\"null\"".equals(menu.getImage_alt()) || "null".equals(menu.getImage_alt())) menu.setImage_alt("");
			if("\"null\"".equals(menu.getCss_class()) || "null".equals(menu.getCss_class())) menu.setCss_class("");
			if("\"null\"".equals(menu.getDescription()) || "null".equals(menu.getDescription())) menu.setDescription("");
			if("\"null\"".equals(menu.getDisplay_yn()) || "null".equals(menu.getDisplay_yn())) menu.setDisplay_yn("");
			if("\"null\"".equals(menu.getUrl_alias()) || "null".equals(menu.getUrl_alias())) menu.setUrl_alias("");
			
			menuService.updateMenu(menu);
			menuList.addAll(menuService.getListMenu(null));
			
			menuTree = getMenuTree(menuList);
			log.info("@@ menuTree = {} ", menuTree);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		map.put("menuTree", menuTree);
		return map;
	}
	
	/**
	 * 메뉴 위로/아래로 수정
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-update-move-menu.do")
	@ResponseBody
	public Map<String, Object> ajaxUpdateMoveMenu(HttpServletRequest request, Menu menu) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String menuTree = null;
		List<Menu> menuList = new ArrayList<>();
		menuList.add(getRootMenu());
		try {
			log.info("@@ menu = {} ", menu);
			if(menu.getMenu_id() == null || menu.getMenu_id().longValue() == 0l
					|| menu.getView_order() == null || menu.getView_order().intValue() == 0
					|| menu.getUpdate_type() == null || "".equals(menu.getUpdate_type())) {
				
				menuList.addAll(menuService.getListMenu(null));
				result = "menu.invalid";
				menuTree = getMenuTree(menuList);
				map.put("result", result);
				map.put("menuTree", menuTree);
				return map;
			}
			
			menuService.updateMoveMenu(menu);
			menuList.addAll(menuService.getListMenu(null));
			
			menuTree = getMenuTree(menuList);
			log.info("@@ menuTree = {} ", menuTree);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		map.put("menuTree", menuTree);
		return map;
	}
	
	/**
	 * 메뉴 삭제
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-delete-menu.do")
	@ResponseBody
	public Map<String, Object> ajaxDeleteMenu(HttpServletRequest request, Menu menu) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String menuTree = null;
		List<Menu> menuList = new ArrayList<>();
		menuList.add(getRootMenu());
		try {
			log.info("@@ menu = {} ", menu);
			if(menu.getMenu_id() == null || menu.getMenu_id().longValue() == 0l) {
				
				menuList.addAll(menuService.getListMenu(null));
				
				result = "menu.invalid";
				menuTree = getMenuTree(menuList);
				map.put("result", result);
				map.put("menuTree", menuTree);
				return map;
			}
			
			menuService.deleteMenu(menu.getMenu_id());
			menuList.addAll(menuService.getListMenu(null));
			
			menuTree = getMenuTree(menuList);
			log.info("@@ menuTree = {} ", menuTree);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		map.put("menuTree", menuTree);
		return map;
	}
	
	/**
	 * 기본 메뉴 트리
	 * @return
	 */
	private Menu getRootMenu() {
		Policy policy = CacheManager.getPolicy();
		
		Menu menu = new Menu();
		menu.setMenu_id(0l);
		if(policy.getContent_menu_group_root() != null && !"".equals(policy.getContent_menu_group_root())) {
			menu.setName(policy.getContent_menu_group_root());
		} else {
			menu.setName("TOP");
		}
		menu.setName_en("TOP");
		menu.setOpen("true");
		menu.setNode_type("company");
		menu.setParent(-1l);
		menu.setParent_name("");
		menu.setView_order(0);
		menu.setDepth(0);
		menu.setDefault_yn("Y");
		menu.setUse_yn("Y");
		menu.setUrl("");
		menu.setImage("");
		menu.setImage_alt("");
		menu.setCss_class("");
		menu.setDescription("");
		
		return menu;
	}
	
	private String getMenuTree(List<Menu> menuList) {
		StringBuilder builder = new StringBuilder(256);
		
		int menuCount = menuList.size();
		Menu menu = menuList.get(0);
		
		builder.append("[");
		builder.append("{");
		
		builder.append("\"menu_id\"").append(":").append("\"" + menu.getMenu_id() + "\"").append(",");
		builder.append("\"name\"").append(":").append("\"" + menu.getName() + "\"").append(",");
		builder.append("\"name_en\"").append(":").append("\"" + menu.getName_en() + "\"").append(",");
		builder.append("\"open\"").append(":").append("\"" + menu.getOpen() + "\"").append(",");
		builder.append("\"node_type\"").append(":").append("\"" + menu.getNode_type() + "\"").append(",");
		builder.append("\"parent\"").append(":").append("\"" + menu.getParent() + "\"").append(",");
		builder.append("\"parent_name\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getParent_name()) + "\"").append(",");
		builder.append("\"view_order\"").append(":").append("\"" + menu.getView_order() + "\"").append(",");
		builder.append("\"depth\"").append(":").append("\"" + menu.getDepth() + "\"").append(",");
		builder.append("\"default_yn\"").append(":").append("\"" + menu.getDefault_yn() + "\"").append(",");
		builder.append("\"use_yn\"").append(":").append("\"" + menu.getUse_yn() + "\"").append(",");
		builder.append("\"display_yn\"").append(":").append("\"" + menu.getDisplay_yn() + "\"").append(",");
		builder.append("\"url\"").append(":").append("\"" + menu.getUrl() + "\"").append(",");
		builder.append("\"url_alias\"").append(":").append("\"" + menu.getUrl_alias() + "\"").append(",");
		builder.append("\"image\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getImage()) + "\"").append(",");
		builder.append("\"image_alt\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getImage_alt()) + "\"").append(",");
		builder.append("\"css_class\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getCss_class()) + "\"").append(",");
		builder.append("\"description\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getDescription()) + "\"");
	
		if(menuCount > 1) {
			long preParent = menu.getParent();
			int preDepth = menu.getDepth();
			int bigParentheses = 0;
			for(int i=1; i<menuCount; i++) {
				menu = menuList.get(i);
				
				if(preParent == menu.getParent()) {
					// 부모가 같은 경우
					builder.append("}");
					builder.append(",");
				} else {
					if(preDepth > menu.getDepth()) {
						// 닫힐때
						int closeCount = preDepth - menu.getDepth();
						for(int j=0; j<closeCount; j++) {
							builder.append("}");
							builder.append("]");
							bigParentheses--;
						}
						builder.append("}");
						builder.append(",");
					} else {
						// 열릴때
						builder.append(",");
						builder.append("\"subTree\"").append(":").append("[");
						bigParentheses++;
					}
				} 
				
				builder.append("{");
				builder.append("\"menu_id\"").append(":").append("\"" + menu.getMenu_id() + "\"").append(",");
				builder.append("\"name\"").append(":").append("\"" + menu.getName() + "\"").append(",");
				builder.append("\"name_en\"").append(":").append("\"" + menu.getName_en() + "\"").append(",");
				builder.append("\"open\"").append(":").append("\"" + menu.getOpen() + "\"").append(",");
				builder.append("\"node_type\"").append(":").append("\"" + menu.getNode_type() + "\"").append(",");
				builder.append("\"parent\"").append(":").append("\"" + menu.getParent() + "\"").append(",");
				builder.append("\"parent_name\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getParent_name()) + "\"").append(",");
				builder.append("\"view_order\"").append(":").append("\"" + menu.getView_order() + "\"").append(",");
				builder.append("\"depth\"").append(":").append("\"" + menu.getDepth() + "\"").append(",");
				builder.append("\"default_yn\"").append(":").append("\"" + menu.getDefault_yn() + "\"").append(",");
				builder.append("\"use_yn\"").append(":").append("\"" + menu.getUse_yn() + "\"").append(",");
				builder.append("\"display_yn\"").append(":").append("\"" + menu.getDisplay_yn() + "\"").append(",");
				builder.append("\"url\"").append(":").append("\"" + menu.getUrl() + "\"").append(",");
				builder.append("\"url_alias\"").append(":").append("\"" + menu.getUrl_alias() + "\"").append(",");
				builder.append("\"image\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getImage()) + "\"").append(",");
				builder.append("\"image_alt\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getImage_alt()) + "\"").append(",");
				builder.append("\"css_class\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getCss_class()) + "\"").append(",");
				builder.append("\"description\"").append(":").append("\"" + StringUtil.getDefaultValue(menu.getDescription()) + "\"");
				
				if(i == (menuCount-1)) {
					// 맨 마지막의 경우 괄호를 닫음
					if(bigParentheses == 0) {
						builder.append("}");
					} else {
						for(int k=0; k<bigParentheses; k++) {
							builder.append("}");
							builder.append("]");
						}
					}
				}
				
				preParent = menu.getParent();
				preDepth = menu.getDepth();
			}
		}
		
		builder.append("}");
		builder.append("]");
		
		return builder.toString();
	}
}
