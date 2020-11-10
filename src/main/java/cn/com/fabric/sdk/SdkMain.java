package cn.com.fabric.sdk;

import org.hyperledger.fabric.sdk.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * fabric-sdk-java测试
 */
public class SdkMain {

    //资源路径
    private static final String baseUrl = "/Users/gaoshudian/work/developer/workspace/personal/myworkspace/fabric-sdk-java-demo/src/main/resources/";

    //用户私钥和证书
    private static final String keyFilePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/e9163e7dfbd34e95f205a87e316ba9f69f5f50ff53d149edb81e0d98b13ad835_sk";
    private static final String certFilePath = baseUrl + "crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts/Admin@org1.example.com-cert.pem";

    //创建channel所需的tx文件
    private static final String txfilePath = baseUrl + "channel.tx";

    //tls连接用的证书
    private static final String TLS_ORDER_FILE = baseUrl + "crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem";
    private static final String TLS_PEER_FILE = baseUrl + "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/tlscacerts/tlsca.org1.example.com-cert.pem";

    private static final String TLS_PEER_FILEAddtion = baseUrl + "crypto-config/peerOrganizations/org2.example.com/tlsca/tlsca.org2.example.com-cert.pem";

    //chaincode路径
    private static final String chaincodePath = baseUrl + "chaincode";

    public FabricClient getFabricClient() throws Exception {
        UserContext userContext = new UserContext();
        userContext.setAffiliation("Org1");//组织机构
        userContext.setMspId("Org1MSP");
        userContext.setAccount("Admin");
        userContext.setName("Admin");
        Enrollment enrollment = UserUtils.getEnrollment(keyFilePath, certFilePath);
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        return fabricClient;
    }

    //创建channel
    @Test
    public void createChannel() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Orderer orderer = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", TLS_ORDER_FILE);
        Channel channel = fabricClient.createChannel("testchannel", orderer, txfilePath);
        System.out.println(channel);
    }

    //获取已有的channel,将peer节点加入应用通道
    @Test
    public void joinPeer() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Orderer orderer = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", TLS_ORDER_FILE);
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        Channel channel = fabricClient.getChannel("testchannel");
        channel.addOrderer(orderer);
        channel.joinPeer(peer);//将peer加入到channel中
        channel.initialize();//初始化channel
        System.out.println(channel);
    }

    //安装合约(一个组织机构的admin用户只能安装同一组织机构下的peer节点)
    @Test
    public void chaincodeInstall() throws Exception {

        FabricClient fabricClient = getFabricClient();
        Peer peer0 = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer0);
        fabricClient.installChaincode(TransactionRequest.Type.GO_LANG, "basicinfo", "1.0", chaincodePath, "basicinfo", peers);
    }

    //合约实例化
    @Test
    public void chaincodeInstantiation() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        Orderer order = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", TLS_ORDER_FILE);
        String initArgs[] = {""};
        fabricClient.initChaincode("testchannel", TransactionRequest.Type.GO_LANG, "basicinfo", "1.0", order, peer, "init", initArgs);
    }

    //合约升级,注意每次改了chaincode以后，如果要升级合约，必须要重新先安装一下chaincode
    @Test
    public void chaincodeUpgrade() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        Orderer order = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", TLS_ORDER_FILE);
        String initArgs[] = {""};
        fabricClient.upgradeChaincode("mychannel", TransactionRequest.Type.GO_LANG, "basicinfo", "2.0", order, peer, "init", initArgs);
    }

    //调用合约
    @Test
    public void chaincodeInvoke() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        Orderer order = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", TLS_ORDER_FILE);
        String initArgs[] = {"110114", "{\"name\":\"zhangsan\",\"identity\":\"110114\",\"mobile\":\"18910012222\"}"};
        fabricClient.invoke("testchannel", TransactionRequest.Type.GO_LANG, "basicinfo", order, peers, "save", initArgs);
    }

    //查询合约
    @Test
    public void chaincodeQuery() throws Exception {
        FabricClient fabricClient = getFabricClient();
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        String initArgs[] = {"110114"};
        Map map = fabricClient.queryChaincode(peers, "testchannel", TransactionRequest.Type.GO_LANG, "basicinfo", "query", initArgs);
        System.out.println(map);
    }

    //注册用户
    @Test
    public void registryUser() throws Exception {
        FabricCAClient caClient = new FabricCAClient("http://172.16.216.130:7054", null);

        //注册admin用户
        Enrollment adminEnrollment = caClient.enroll("admin", "pass");
        UserContext adminUser = new UserContext();
        adminUser.setName("admin");
        adminUser.setAffiliation("org1");
        adminUser.setEnrollment(adminEnrollment);

        //注册普通用户
        UserContext user = new UserContext();
        user.setName("xiaogao");
        user.setAffiliation("org1");
        String secret = caClient.register(adminUser, user);
        System.out.println(secret);//PTynLlvwuFlv

        Enrollment userEnrollment = caClient.enroll("xiaogao",secret);
        System.out.println(userEnrollment.getCert());
        System.out.println(userEnrollment.getKey());
    }

    //注册用户查询合约
    @Test
    public void caQueryChaincode() throws Exception {
        FabricCAClient caClient = new FabricCAClient("http://172.16.216.130:7054",null);
        Enrollment enrollment = caClient.enroll("xiaogao","PTynLlvwuFlv");

        UserContext userContext = new UserContext();
        userContext.setAffiliation("Org1");
        userContext.setMspId("Org1MSP");
        userContext.setAccount("Admin");
        userContext.setName("Admin");
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);
        String initArgs[] = {"110120"};
        Map map =  fabricClient.queryChaincode(peers,"testchannel", TransactionRequest.Type.GO_LANG,"basicinfo","query",initArgs);
        System.out.println(map);
    }


    //注册用户invoke合约
    @Test
    public void caInvokeChaincode() throws Exception {

        FabricCAClient caClient = new FabricCAClient("http://172.16.216.130:7054",null);
        Enrollment enrollment = caClient.enroll("xiaogao","PTynLlvwuFlv");

        UserContext userContext = new UserContext();
        userContext.setAffiliation("Org1");
        userContext.setMspId("Org1MSP");
        userContext.setAccount("Admin");
        userContext.setName("Admin");
        userContext.setEnrollment(enrollment);
        FabricClient fabricClient = new FabricClient(userContext);
        Peer peer = fabricClient.getPeer("peer0.org1.example.com", "grpcs://peer0.org1.example.com:7051", TLS_PEER_FILE);
        List<Peer> peers = new ArrayList<>();
        peers.add(peer);

        Orderer order = fabricClient.getOrderer("orderer.example.com", "grpcs://orderer.example.com:7050", TLS_ORDER_FILE);
        String initArgs[] = {"110114", "{\"name\":\"zhangsan\",\"identity\":\"110114\",\"mobile\":\"18910012222\"}"};
        fabricClient.invoke("testchannel", TransactionRequest.Type.GO_LANG, "basicinfo", order, peers, "save", initArgs);
    }

}