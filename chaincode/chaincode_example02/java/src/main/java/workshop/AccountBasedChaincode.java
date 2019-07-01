package com.thoughtworks.fabric.workshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.fabric.workshop.model.Wallet;
import org.apache.commons.lang.StringUtils;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

import static com.thoughtworks.fabric.workshop.model.ChaincodeResponse.error;
import static com.thoughtworks.fabric.workshop.model.ChaincodeResponse.success;

public class AccountBasedChaincode extends ChaincodeBase {

    @Override
    public Response init(ChaincodeStub stub) {
        return newSuccessResponse(success("Init success"));
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        final String func = stub.getFunction();
        final List<String> parameters = stub.getParameters();
        switch (func) {
            case "createWallet":
                return createWallet(stub, parameters);
            case "getWallet":
                return getWallet(stub, parameters);
            case "transfer":
                return transfer(stub, parameters);
            default:
                return newErrorResponse(error("Unsupported method", ""));
        }
    }

    private Response transfer(ChaincodeStub stub, List<String> parameters) {
        if (parameters.size() != 3)
            return newErrorResponse(error("Incorrect number of arguments, expecting 3", ""));
        final String fromWalletId = parameters.get(0);
        final String toWalletId = parameters.get(1);
        final String tokenAmount = parameters.get(2);
        if (StringUtils.isEmpty(fromWalletId) || StringUtils.isEmpty(toWalletId) || StringUtils.isEmpty(tokenAmount))
            return newErrorResponse(error("Invalid argument(s)", ""));
        if (fromWalletId.equals(toWalletId))
            return newErrorResponse(error("From-wallet is same as to-wallet", ""));

        double tokenAmountDouble;
        try {
            tokenAmountDouble = Double.parseDouble(tokenAmount);
            if (tokenAmountDouble < 0d) {
                return newErrorResponse(error("Invalid token amount", ""));
            }
        } catch (NumberFormatException e) {
            return newErrorResponse(error("Token amount parse error", ""));
        }

        try {
            String fromWalletString = stub.getStringState(fromWalletId);
            if (StringUtils.isEmpty(fromWalletString))
                return newErrorResponse(error("Nonexistent from-wallet", ""));
            String toWalletString = stub.getStringState(toWalletId);
            if (StringUtils.isEmpty(toWalletString))
                return newErrorResponse(error("Nonexistent to-wallet", ""));

            ObjectMapper objectMapper = new ObjectMapper();
            Wallet fromWallet = objectMapper.readValue(fromWalletString, Wallet.class);
            Wallet toWallet = objectMapper.readValue(toWalletString, Wallet.class);

            if (fromWallet.getTokenAmount() < tokenAmountDouble)
                return newErrorResponse(error("Token amount not enough", ""));

            fromWallet.setTokenAmount(fromWallet.getTokenAmount() - tokenAmountDouble);
            toWallet.setTokenAmount(toWallet.getTokenAmount() + tokenAmountDouble);
            stub.putState(fromWalletId, objectMapper.writeValueAsBytes(fromWallet));
            stub.putState(toWalletId, objectMapper.writeValueAsBytes(toWallet));

            return newSuccessResponse(success("Transferred success"));
        } catch (Throwable e) {
            return newErrorResponse(error(e.getMessage(), ""));
        }
    }

    private Response getWallet(ChaincodeStub stub, List<String> parameters) {
        if (parameters.size() != 1) {
            return newErrorResponse(error("Incorrect number of parameters, expected 1", ""));
        }
        final String walletId = parameters.get(0);
        if (StringUtils.isEmpty(walletId)) {
            return newErrorResponse(error("Invalid argument", ""));
        }
        try {
            final String walletString = stub.getStringState(walletId);
            if (StringUtils.isEmpty(walletString)) {
                return newErrorResponse(error("Invalid argument", ""));
            }
            return newSuccessResponse(new ObjectMapper().writeValueAsBytes(success(walletString)));
        } catch (JsonProcessingException e) {
            return newErrorResponse(error(e.getMessage(), ""));
        }
    }

    private Response createWallet(ChaincodeStub stub, List<String> parameters) {
        if (parameters.size() != 2) {
            return newErrorResponse(error("Incorrect number of parameters, expected 2", ""));
        }

        final String walletId = parameters.get(0);
        final String tokenAmount = parameters.get(1);
        if (StringUtils.isEmpty(walletId) || StringUtils.isEmpty(tokenAmount)) {
            return newErrorResponse(error("Invalid argument(s)", ""));
        }

        double tokenAmountDouble;
        try {
            tokenAmountDouble = Double.parseDouble(tokenAmount);
            if (tokenAmountDouble < 0d) {
                return newErrorResponse(error("Invalid token amount", ""));
            }
        } catch (NumberFormatException e) {
            return newErrorResponse(error("Token amount parse error", ""));
        }

        final Wallet wallet = new Wallet(walletId, tokenAmountDouble);
        try {
            if (StringUtils.isNotEmpty(stub.getStringState(walletId))) {
                return newErrorResponse(error("Wallet existed", ""));
            }
            stub.putState(walletId, (new ObjectMapper().writeValueAsBytes(wallet)));
            return newSuccessResponse(success("Wallet created"));
        } catch (JsonProcessingException e) {
            return newErrorResponse(error(e.getMessage(), ""));
        }
    }

    public static void main(String[] args) {
        new AccountBasedChaincode().start(args);
    }
}
