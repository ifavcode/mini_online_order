package cn.guetzjb.onlineorderingjava.utils;

import cn.guetzjb.onlineorderingjava.config.ServerProperties;
import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UploadUtil {
    private static ChannelSftp sftp = null;

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     */
    public static String uploadFile(String dirName, String filename, InputStream input) {
        FTPClient ftp = new FTPClient();
        String url = "";
        try {
            JSch jsch = new JSch();
            ServerProperties instance = ServerProperties.getInstance();
            //获取sshSession  账号-ip-端口
            Session sshSession = jsch.getSession(instance.getUsername(), instance.getIp(), instance.getPort());
            //添加密码
            sshSession.setPassword(instance.getPassword());
            Properties sshConfig = new Properties();
            //严格主机密钥检查
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            //开启sshSession链接
            sshSession.connect();
            //获取sftp通道
            Channel channel = sshSession.openChannel("sftp");
            //开启
            channel.connect();
            sftp = (ChannelSftp) channel;
            //服务器路径
            String newDir = "/opt/files/" + dirName;
            if (!isExistDir(newDir, sftp)) {
                sftp.mkdir(newDir);
            }
            //设置为被动模式
            ftp.enterLocalPassiveMode();
            //设置上传文件的类型为二进制类型
            //进入到要上传的目录  然后上传文件
            sftp.cd(instance.getDir() + dirName);
            sftp.put(input, filename);
            url += "https://www.guetzjb.cn/assets_other/" + (dirName.equals("") ? dirName : (dirName + "/")) + filename;
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
        }
        return url;
    }


    private static boolean isExistDir(String path, ChannelSftp sftp) {
        boolean isExist = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(path);
            isExist = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isExist = false;
            }
        }
        return isExist;

    }

}

