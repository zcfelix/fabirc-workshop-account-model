package com.thoughtworks.fabric.workshop.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Wallet {
    private String walletId;
    private Double tokenAmount;
}
