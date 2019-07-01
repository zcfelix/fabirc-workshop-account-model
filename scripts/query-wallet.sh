#!/usr/bin/env bash

peer chaincode query -C mychannel -n mycc -c '{"Args":["getWallet","felix"]}'

peer chaincode query -C mychannel -n mycc -c '{"Args":["getWallet","alex"]}'