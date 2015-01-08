package ua.sergiishapoval.carrental.controller;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.sergiishapoval.carrental.command.Command;
import ua.sergiishapoval.carrental.command.CommandFactory;
import ua.sergiishapoval.carrental.dao.DaoInitDestroy;
import ua.sergiishapoval.carrental.dao.DaoFactory;
import ua.sergiishapoval.carrental.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Сергей on 26.12.2014.
 */
//@WebServlet(name = "MainServlet", urlPatterns = "/*", loadOnStartup = 1)
public class MainServlet extends HttpServlet {
    
    private Logger logger = LoggerFactory.getLogger(MainServlet.class);

    @Override
    public void init() throws ServletException {
        DaoInitDestroy daoInitDestroy = null;
        try {
            daoInitDestroy = DaoFactory.getDaoInitDestroy();
            daoInitDestroy.initDB();
        } catch (SQLException e) {
            logger.error("DBError", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Command command = CommandFactory.createCommand(request);
        command.execute(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*clearing invalid user in session begin*/
        if (request.getSession().getAttribute("userError") != null) {
            request.getSession().removeAttribute("user");
            request.getSession().removeAttribute("auth");
            request.getSession().removeAttribute("userError");
        }
        /*clearing invalid user in session end*/
        
        /*set Locale start*/
        if (request.getSession().getAttribute("lang_id") == null){
            String language = request.getLocale().getLanguage();
            switch (language.toLowerCase()){
                case "ru": break;
                default:language = "en";
            }
            request.getSession().setAttribute("lang_id", language );
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
        }
        /*set Locale end*/
        
        
        /*Authorize through cookie start*/
        Cookie[] cookies = request.getCookies();
        if (cookies!= null && cookies.length > 1) {
            Map<String, String> userMap = new HashMap<>();

            for (Cookie cookie : cookies) {
                userMap.put(cookie.getName(), cookie.getValue());
            }

            User user = new User();

            try {
                BeanUtils.populate(user, userMap);
            } catch (IllegalAccessException e) {
                logger.error("BeanUtilsError", e);
            } catch (InvocationTargetException e) {
                logger.error("BeanUtilsError", e);
            }
            request.getSession().setAttribute("user", user);
        }
        /*Authorize through cookie end*/

        Command command = CommandFactory.createCommand(request);
        command.execute(request, response);
    }

    @Override
    public void destroy() {
        DaoInitDestroy daoInitDestroy = null;
        try {
            daoInitDestroy = DaoFactory.getDaoInitDestroy();
            daoInitDestroy.destroyDB();
        } catch (SQLException e) {
            logger.error("DBError", e);

        }
    }
}
