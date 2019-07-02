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
            default:
                return newErrorResponse(error("Unsupported method", ""));
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
