package io.github.sakurawald.module.initializer.functional.enchantment.config.model;

public class EnchantmentConfigModel {

    public Enchantment enchantment = new Enchantment();

    public static class Enchantment {

        public OverridePower override_power = new OverridePower();

        public static class OverridePower {

            public int power_provider_amount = 15;
        }
    }
}
