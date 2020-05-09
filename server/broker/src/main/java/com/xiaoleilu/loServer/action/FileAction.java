package com.xiaoleilu.loServer.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.LoggerFactory;

import com.playcrab.util.StringUtils;
import com.playcrab.util.TimeUtils;
import com.xiaoleilu.loServer.ServerSetting;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

/**
 * 默认的主页Action，当访问主页且没有定义主页Action时，调用此Action
 * 
 * @author Looly
 *
 */
public class FileAction extends Action {
    private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(FileAction.class);

    @Override
    public boolean action(Request request, Response response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        if (false == Request.METHOD_GET.equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED, "Please use GET method to request file!");
            return true;
        }

        if(ServerSetting.isRootAvailable() == false){
            response.sendError(HttpResponseStatus.NOT_FOUND, "404 Root dir not avaliable!");
            return true;
        }

        final File file = getFileByPath(request.getPath());
        Logger.debug("Client [{}] get file [{}]", request.getIp(), file.getPath());

        // 隐藏文件，跳过
        if (file.isHidden() || !file.exists()) {
            response.sendError(HttpResponseStatus.NOT_FOUND, "404 File not found!");
            return true;
        }

        // 非文件，跳过
        if (false == file.isFile()) {
            response.sendError(HttpResponseStatus.FORBIDDEN, "403 Forbidden!");
            return true;
        }

        // Cache Validation
        String ifModifiedSince = request.getHeader(HttpHeaderNames.IF_MODIFIED_SINCE.toString());
        if (StringUtils.isNullOrEmpty(ifModifiedSince)) {
            long ifModifiedSinceDate = TimeUtils.parseHttpDateTime(ifModifiedSince);
            if(ifModifiedSinceDate > 0L) {
                // 只对比到秒一级别
                long ifModifiedSinceDateSeconds = ifModifiedSinceDate / 1000;
                long fileLastModifiedSeconds = file.lastModified() / 1000;
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    Logger.debug("File {} not modified.", file.getPath());
                    response.sendNotModified();
                    return true;
                }
            }
        }

        response.setContent(file);
        return true;
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	
	/**
	 * 通过URL中的path获得文件的绝对路径
	 * 
	 * @param httpPath Http请求的Path
	 * @return 文件绝对路径
	 */
	public static File getFileByPath(String httpPath) {
		// Decode the path.
		try {
			httpPath = URLDecoder.decode(httpPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		if (httpPath.isEmpty() || httpPath.charAt(0) != '/') {
			return null;
		}

		// 路径安全检查
		if (httpPath.contains("/.") || httpPath.contains("./") || httpPath.charAt(0) == '.' || httpPath.charAt(httpPath.length() - 1) == '.' || StringUtils
            .isMatch(INSECURE_URI, httpPath)) {
			return null;
		}

		// 转换为绝对路径
		return Paths.get(ServerSetting.getRoot().getPath(), httpPath).toFile();
	}
}
