package cn.com.fabric.sdk;

import cn.com.fabric.sdk.constant.Constants;
import cn.com.fabric.sdk.user.FabricCAClient;
import cn.com.fabric.sdk.user.UserContext;
import cn.com.fabric.sdk.user.UserUtils;
import org.hyperledger.fabric.sdk.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * fabric-sdk-java测试
 */
public class SdkMain {

    //获取FabricClient
    public FabricClient getFabricClient() throws Exception {
        UserContext userContext = new UserContext();
        userContext.setAffiliation("Org1");//组织机构
        userContext.setMspId("Org1MSP");//用户所在的组织机构的mspId
        userContext.setAccount("Admin");
        userContext.setName("Admin");//用来标示用户
        Enrollment enrollment = UserUtils.getEnrollment(Constants.KEY_FILE_PATH, Constants.CERT_FILE_PATH);
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        return fabricClient;
    }

    //创建channel
    @Test
    public void createChannel() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Orderer orderer = fabricClient.getOrderer(Constants.ORDER_NAME, Constants.ORDER_GRPC_URL,
            Constants.TLS_ORDER_FILE);
        Channel channel = fabricClient.createChannel(Constants.CHANNEL_ID, orderer, Constants.txfilePath);
        System.out.println(channel);
    }

    //获取已有的channel,将peer节点加入应用通道
    @Test
    public void joinPeer() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Orderer orderer = fabricClient.getOrderer(Constants.ORDER_NAME, Constants.ORDER_GRPC_URL,
            Constants.TLS_ORDER_FILE);
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        Channel channel = fabricClient.getChannel(Constants.CHANNEL_ID);
        channel.addOrderer(orderer);
        channel.joinPeer(peer);//将peer加入到channel中
        channel.initialize();//初始化channel
        System.out.println(channel);
    }

    //安装合约(一个组织机构的admin用户只能安装同一组织机构下的peer节点)
    @Test
    public void chaincodeInstall() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Peer peer0 = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer0);
        fabricClient.installChaincode(Constants.CHAINCODE_SOURCE_LOCATION, Constants.CHAINCODE_PATH,
            "basicInfo", "1.0", peers);
    }

    //合约实例化
    @Test
    public void chaincodeInstantiation() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        Orderer order = fabricClient.getOrderer(Constants.ORDER_NAME, Constants.ORDER_GRPC_URL, Constants.TLS_ORDER_FILE);
        String initArgs[] = {""};
        fabricClient.initChaincode(Constants.CHANNEL_ID, "basicInfo", "1.0", order, peer, "init", initArgs);
    }

    //合约升级,注意每次改了chaincode以后，如果要升级合约，必须要重新先安装一下chaincode
    @Test
    public void chaincodeUpgrade() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        Orderer order = fabricClient.getOrderer(Constants.ORDER_NAME, Constants.ORDER_GRPC_URL, Constants.TLS_ORDER_FILE);
        String initArgs[] = {""};
        fabricClient.upgradeChaincode(Constants.CHANNEL_ID, "basicInfo", "2.0", order, peer, "init", initArgs);
    }

    //调用合约
    @Test
    public void chaincodeInvoke() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        Orderer order = fabricClient.getOrderer(Constants.ORDER_NAME, Constants.ORDER_GRPC_URL, Constants.TLS_ORDER_FILE);
        String initArgs[] = {"110115", "{\"name\":\"高书电\",\"identity\":\"110114\",\"mobile\":\"18910012222\"}"};
        fabricClient.invoke(Constants.CHANNEL_ID, "basicInfo", order, peers, "save", initArgs);
    }

    //查询合约
    @Test
    public void chaincodeQuery() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        String initArgs[] = {"110114"};
        Map map = fabricClient.queryChaincode(peers, Constants.CHANNEL_ID, "basicInfo", "query", initArgs);
        System.out.println(map);
    }

    //注册用户
    @Test
    public void registryUser() throws Exception {
        FabricCAClient caClient = new FabricCAClient("http://172.16.216.2:7054", null);

        //获取admin用户凭证
        Enrollment adminEnrollment = caClient.enroll("admin", "adminpw");
        UserContext adminUser = new UserContext();
        adminUser.setName("admin");
        adminUser.setAffiliation("org1");
        adminUser.setEnrollment(adminEnrollment);

        //注册普通用户
        UserContext user = new UserContext();
        user.setName("xiaogao");
        user.setAffiliation("org1");
        String secret = caClient.register(adminUser, user);
        System.out.println("密码："+secret);//PTynLlvwuFlv

        //enroll()方法可以理解为登录功能
        Enrollment userEnrollment = caClient.enroll("xiaogao", secret);
        System.out.println(userEnrollment.getCert());
        System.out.println(userEnrollment.getKey());
    }

    //注册用户查询合约
    @Test
    public void caQueryChaincode() throws Exception {
        FabricCAClient caClient = new FabricCAClient("http://172.16.216.2:7054", null);
        Enrollment enrollment = caClient.enroll("xiaogao", "AUEFzlffgPFT");

        //UserContext中的mspId,name,enrollment这3个属性不能错，affiliation和account无所谓，随便填
        UserContext userContext = new UserContext();
        userContext.setAffiliation("org1");
        userContext.setMspId("Org1MSP");
        userContext.setAccount("小高");
        userContext.setName("xiaogao");
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        String initArgs[] = {"110116"};
        Map map = fabricClient.queryChaincode(peers, Constants.CHANNEL_ID, "basicInfo", "query", initArgs);
        System.out.println("执行结果:"+map);
    }


    //注册用户invoke合约
    @Test
    public void caInvokeChaincode() throws Exception {

        FabricCAClient caClient = new FabricCAClient("http://172.16.216.2:7054", null);
        Enrollment enrollment = caClient.enroll("xiaogao", "AUEFzlffgPFT");

        UserContext userContext = new UserContext();
        userContext.setAffiliation("组织机构1，名称随便填");
        userContext.setMspId("Org1MSP");
        userContext.setAccount("小高");
        userContext.setName("xiaogao");
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        Peer peer = fabricClient.getPeer(Constants.PEER_NAME, Constants.PEER_GRPC_URL, Constants.TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);

        Orderer order = fabricClient.getOrderer(Constants.ORDER_NAME, Constants.ORDER_GRPC_URL,
            Constants.TLS_ORDER_FILE);
        String initArgs[] = {"110116", "{\"name\":\"老高2\",\"identity\":\"110114\",\"mobile\":\"18910012222\"}"};
        fabricClient.invoke(Constants.CHANNEL_ID, "basicInfo", order, peers, "save", initArgs);
    }
}