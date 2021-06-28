package cn.com.fabric.sdk;

import cn.com.fabric.sdk.user.UserContext;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.*;

public class FabricClient {

    private static final Logger log = LoggerFactory.getLogger(FabricClient.class);

    private HFClient hfClient;

    public FabricClient(UserContext userContext) throws Exception {
        hfClient = HFClient.createNewInstance();
        //加密算法
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        hfClient.setCryptoSuite(cryptoSuite);
        hfClient.setUserContext(userContext);
    }

    /**
     * 创建channel
     * channelName channel的名字
     * order       order的信息
     * txPath      创建channel所需的tx文件
     */
    public Channel createChannel(String channelName, Orderer order, String txPath) throws Exception {
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(txPath));
        return hfClient.newChannel(channelName, order, channelConfiguration,
            hfClient.getChannelConfigurationSignature(channelConfiguration, hfClient.getUserContext()));
    }

    //获取已有的channel
    public Channel getChannel(String channelName) throws Exception {
        return hfClient.newChannel(channelName);
    }

    /**
     * 安装合约
     * chaincodeLocation 合约的目录路径
     * CHAINCODE_PATH     合约的文件夹
     * chaincodeName     合约名称
     * chaincodeVersion  合约版本
     * peers             安装的peers 节点,一个组织机构的admin用户只能安装同一组织机构下的peer节点
     */
    public void installChaincode(String chaincodeLocation, String chaincodePath,
                                 String chaincodeName, String chaincodeVersion, List<Peer> peers) throws Exception {

        ChaincodeID.Builder builder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(chaincodeVersion);
        InstallProposalRequest installProposalRequest = hfClient.newInstallProposalRequest();
        installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);//chaincode开发语言
        installProposalRequest.setChaincodeID(builder.build());
        installProposalRequest.setChaincodeSourceLocation(new File(chaincodeLocation));
        installProposalRequest.setChaincodePath(chaincodePath);
        Collection<ProposalResponse> responses = hfClient.sendInstallProposal(installProposalRequest, peers);
        for (ProposalResponse response : responses) {
            if (response.getStatus().getStatus() == 200) {
                log.info("{} installed sucess", response.getPeer().getName());
            } else {
                log.error("{} installed fail", response.getMessage());
            }
        }
    }

    //合约的实例化
    public void initChaincode(String channelName, String chaincodeName, String chaincodeVersion, Orderer order,
                              Peer peer, String funcName, String args[]) throws Exception {
        Channel channel = getChannel(channelName);
        channel.addPeer(peer);
        channel.addOrderer(order);
        channel.initialize();

        //构建实例化合约请求对象
        ChaincodeID.Builder builder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(chaincodeVersion);
        InstantiateProposalRequest instantiateProposalRequest = hfClient.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        instantiateProposalRequest.setChaincodeID(builder.build());
        instantiateProposalRequest.setFcn(funcName);
        instantiateProposalRequest.setArgs(args);
        //1.发送提案，背书节点返回响应
        Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest);
        for (ProposalResponse response : responses) {
            if (response.getStatus().getStatus() == 200) {
                log.info("{} init sucess", response.getPeer().getName());
            } else {
                log.error("{} init fail", response.getMessage());
            }
        }
        //2.提交事务
        channel.sendTransaction(responses);
    }

    //合约的升级
    public void upgradeChaincode(String channelName, String chaincodeName, String chaincodeVersion,
                          Orderer order, Peer peer, String funcName, String args[]) throws Exception {

        Channel channel = getChannel(channelName);
        channel.addPeer(peer);
        channel.addOrderer(order);
        channel.initialize();

        ChaincodeID.Builder builder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(chaincodeVersion);
        UpgradeProposalRequest upgradeProposalRequest = hfClient.newUpgradeProposalRequest();
        upgradeProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        upgradeProposalRequest.setChaincodeID(builder.build());
        upgradeProposalRequest.setFcn(funcName);
        upgradeProposalRequest.setArgs(args);

        //设置背书策略
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        String endorsementPolicyFilePath = "/Users/gaoshudian/work/developer/workspace/personal/myworkspace/" +
            "fabric-sdk-java-demo/src/main/resources/endorsementpolicy.yaml";
        chaincodeEndorsementPolicy.fromYamlFile(new File(endorsementPolicyFilePath));
        upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        Collection<ProposalResponse> responses = channel.sendUpgradeProposal(upgradeProposalRequest);

        for (ProposalResponse response : responses) {
            if (response.getStatus().getStatus() == 200) {
                log.info("{} upgrade sucess", response.getPeer().getName());
            } else {
                log.error("{} upgrade fail", response.getMessage());
            }
        }
        channel.sendTransaction(responses);
    }

    //合约的调用
    public void invoke(String channelName, String chaincodeName, Orderer order, List<Peer> peers,
                String funcName, String args[]) throws Exception {
        Channel channel = getChannel(channelName);
        channel.addOrderer(order);
        //如果背书策略是多个peer,则这里必须将所有的背书节点加进来
        for (Peer p : peers) {
            channel.addPeer(p);
        }
        channel.initialize();

        TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        ChaincodeID.Builder builder = ChaincodeID.newBuilder().setName(chaincodeName);
        transactionProposalRequest.setChaincodeID(builder.build());
        transactionProposalRequest.setFcn(funcName);
        transactionProposalRequest.setArgs(args);

        Collection<ProposalResponse> responses = channel.sendTransactionProposal(transactionProposalRequest, peers);

        for (ProposalResponse response : responses) {
            if (response.getStatus().getStatus() == 200) {
                log.info("{} invoke proposal {} sucess", response.getPeer().getName(), funcName);
            } else {
                String logArgs[] = {response.getMessage(), funcName, response.getPeer().getName()};
                log.error("{} invoke proposal {} fail on {}", logArgs);
            }
        }
        channel.sendTransaction(responses);
    }

    //合约的查询
    public Map queryChaincode(List<Peer> peers, String channelName, String chaincodeName,
                       String funcName, String args[]) throws Exception {
        Channel channel = getChannel(channelName);
        for (Peer p : peers) {
            channel.addPeer(p);
        }
        channel.initialize();

        QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
        queryByChaincodeRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        ChaincodeID.Builder builder = ChaincodeID.newBuilder().setName(chaincodeName);
        queryByChaincodeRequest.setChaincodeID(builder.build());
        queryByChaincodeRequest.setFcn(funcName);
        queryByChaincodeRequest.setArgs(args);

        Collection<ProposalResponse> responses = channel.queryByChaincode(queryByChaincodeRequest);

        HashMap map = new HashMap();
        for (ProposalResponse response : responses) {
            if (response.getStatus().getStatus() == 200) {
                log.info("data is {}", response.getProposalResponse().getResponse().getPayload());
                map.put(response.getStatus().getStatus(),
                    new String(response.getProposalResponse().getResponse().getPayload().toByteArray()));
                return map;
            } else {
                log.error("data get error {}", response.getMessage());
                map.put(response.getStatus().getStatus(), response.getMessage());
                return map;
            }
        }
        map.put("code", "404");
        return map;
    }


    //创建orderer对象，内部其实就是new Orderer
    public Orderer getOrderer(String name, String grpcUrl, String tlsFilePath) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("pemFile", tlsFilePath);
        Orderer orderer = hfClient.newOrderer(name, grpcUrl, properties);
        return orderer;
    }

    //创建peer对象
    public Peer getPeer(String name, String grpcUrl, String tlsFilePath) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("pemFile", tlsFilePath);
        return hfClient.newPeer(name, grpcUrl, properties);
    }


}