package controller;

import com.alibaba.fastjson.JSON;
import model.Result;
import service.ProjectService;
import utils.ErrorCons;
import xjtucad.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "GetDataAPI", urlPatterns = {"/api/v1/project"})
public class ProjectServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession();
        User userinfo = (User) httpSession.getAttribute("userInfo");
        Result result = new Result();
        String projectID = req.getParameter("id");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json,charset=UTF-8");
//        resp.getWriter().print(JSON.toJSONString(result));
        boolean notEnd = true;
        try {

            String appName = req.getParameter("appName");
            doFilter(result,appName);
            ProjectService projectService = new ProjectService(userinfo, appName);
            if (appName == null || appName.length() < 1) {
                result.setContent("请传入AppName参数");
                notEnd = false;
            }
            if (projectID != null && projectID.length() > 0) {
                //根据projectID查询
                int id = Integer.valueOf(projectID);
                result = projectService.getProjectbyId(id);
                notEnd = false;
            }


//            String domainAll = req.getParameter("domainAll");
            String domain = req.getParameter("domain");
            if (notEnd && domain != null && domain.length() > 0) {
                //根据群组查询
                result = projectService.getProjectByDomain(domain);
                notEnd = false;
            }

            String tempProjectID = req.getParameter("tempProjectID");
            if (notEnd && tempProjectID != null && tempProjectID.length() > 0) {
                //根据模板ID查询
                result = projectService.getProjectByTempProjectID(tempProjectID);
                notEnd = false;
            }

            String username = req.getParameter("username");
            if (notEnd) {
                //根据userAll查询
                result = projectService.getProjectListbyUsername(username);
                notEnd = false;
            }

            if (notEnd) {
                result.setContent(ErrorCons.PARAMS_ERROR);
            }
        } catch (Exception e) {
            result.setContent(ErrorCons.PARAMS_ERROR);
        } finally {
            resp.getWriter().print(JSON.toJSONString(result));
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession();
        User userinfo = (User) httpSession.getAttribute("userInfo");
        Result result = new Result();
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json,charset=UTF-8");
        //获取对应的参数
        String appName = req.getParameter("appName");
        String projectName = req.getParameter("projectName");

        //判断参数是否正确
        if(appName==null||appName.length()<1||projectName==null||projectName.length()<1){
            result.setContent(ErrorCons.PARAMS_ERROR);
        }else {
            //如果基本参数没有问题，转到服务
            ProjectService projectService = new ProjectService(userinfo, appName);
            result = projectService.newProjectRecord(req.getParameter("projectName"),
                    req.getParameter("memo"),
                    req.getParameter("appResult"),
                    req.getParameter("tempProjectID"),
                    req.getParameter("appContent"),
                    req.getParameter("reservation"));
        }
        resp.getWriter().print(JSON.toJSONString(result));

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession();
        req.setCharacterEncoding("utf-8");
        User userinfo = (User) httpSession.getAttribute("userInfo");
        Result result = new Result();
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json,charset=UTF-8");
        BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String line;
        Map<String,String> params = new HashMap<String, String>();
        while ((line = in.readLine()) != null)
            params.putAll(getPraFromReq(line));
        try {
            //获取要更新的项目ID
            int projectID = Integer.parseInt(params.get("id"));
            String appName = params.get("appName");
            doFilter(result,appName);
            ProjectService projectService = new ProjectService(userinfo, appName);
            result= projectService.updateProjectRecord(projectID,params.get("projectName"),params.get("memo"),
                    params.get("appResult"),params.get  ("tempProjectID"),req.getParameter("appContent"),
                    req.getParameter("reservation"));
        }catch (Exception e){
            e.printStackTrace();
            result.setError(ErrorCons.PARAMS_ERROR);
        }finally {
            resp.getWriter().println(JSON.toJSONString(result));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession();
        User userinfo = (User) httpSession.getAttribute("userInfo");
        Result result = new Result();
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json,charset=UTF-8");
        BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String line;
        Map<String,String> params = new HashMap<String, String>();
        while ((line = in.readLine()) != null)
            params.putAll(getPraFromReq(line));
        try {
            //获取要更新的项目ID
            String appName = params.get("appName");
            doFilter(result,appName);
            int projectID = Integer.parseInt(params.get("id"));
            ProjectService projectService  = new ProjectService(userinfo,appName);
            result= projectService.deleteProjectRecord(projectID);
        }catch (Exception e){
            e.printStackTrace();
            result.setError(ErrorCons.PARAMS_ERROR);
        }finally {
            resp.getWriter().println(JSON.toJSONString(result));
        }
    }

    void doFilter(Result result,String appName) throws Exception {
        if(appName==null||appName.length()<1) {
            throw new Exception(ErrorCons.APPNME_DEFICIENCY);
        }
    }

    Map<String,String> getPraFromReq(String string) throws UnsupportedEncodingException {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrResult = string.split("&");
        if(arrResult!=null&&arrResult.length>0){
            for (String str:arrResult){
                String[] p = str.split("=");
                if(p.length==2&&p[0]!=null&&p[0].length()>0&&p[1]!=null&&p[1].length()>0){
                    mapRequest.put(p[0], URLDecoder.decode(p[1],"UTF-8"));
                }
            }
        }
        return mapRequest;
    }
}