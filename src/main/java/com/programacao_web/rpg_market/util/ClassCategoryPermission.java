package com.programacao_web.rpg_market.util;

import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.UserRole;
import java.util.*;

public class ClassCategoryPermission {
    private static final Map<String, Set<ProductCategory>> classToCategories = new HashMap<>();
    static {
        classToCategories.put("Guerreiro", Set.of(
            ProductCategory.ARMAS,
            ProductCategory.ARMADURA_VESTIMENTA
        ));
        classToCategories.put("Mago", Set.of(
            ProductCategory.POCOES_ELIXIRES,
            ProductCategory.PERGAMINHOS_LIVROS
        ));
        classToCategories.put("Clérigo", Set.of(
            ProductCategory.POCOES_ELIXIRES,
            ProductCategory.DIVERSOS
        ));
        classToCategories.put("Ladino", Set.of(
            ProductCategory.DIVERSOS,
            ProductCategory.ARMAS
        ));
        classToCategories.put("Druida", Set.of(
            ProductCategory.POCOES_ELIXIRES,
            ProductCategory.MONTARIAS_BESTAS
        ));
        classToCategories.put("Bardo", Set.of(
            ProductCategory.JOIAS_ARTEFATOS,
            ProductCategory.PERGAMINHOS_LIVROS
        ));
        classToCategories.put("Paladino", Set.of(
            ProductCategory.ARMADURA_VESTIMENTA,
            ProductCategory.JOIAS_ARTEFATOS
        ));
        classToCategories.put("Ranger", Set.of(
            ProductCategory.ARMAS,
            ProductCategory.MONTARIAS_BESTAS
        ));
    }

    /**
     * Retorna as categorias permitidas para um usuário baseado na sua classe e role
     */
    public static Set<ProductCategory> getAllowedCategories(String characterClass, UserRole userRole) {
        // MESTREs podem ver todas as categorias
        if (userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN) {
            return EnumSet.allOf(ProductCategory.class);
        }
        
        if (characterClass == null || !classToCategories.containsKey(characterClass)) {
            return EnumSet.allOf(ProductCategory.class);
        }
        return classToCategories.get(characterClass);
    }

    /**
     * Método de compatibilidade - usa apenas a classe do personagem
     */
    public static Set<ProductCategory> getAllowedCategories(String characterClass) {
        if (characterClass == null || !classToCategories.containsKey(characterClass)) {
            return EnumSet.allOf(ProductCategory.class);
        }
        return classToCategories.get(characterClass);
    }

    public static boolean isCategoryAllowed(String characterClass, ProductCategory category) {
        return getAllowedCategories(characterClass).contains(category);
    }

    /**
     * Verifica se usuário pode ver uma categoria baseado na classe e role
     */
    public static boolean isCategoryAllowed(String characterClass, UserRole userRole, ProductCategory category) {
        return getAllowedCategories(characterClass, userRole).contains(category);
    }

    /**
     * Verifica se o usuário tem permissões administrativas (MESTRE ou ADMIN)
     */
    public static boolean hasAdminPermissions(UserRole userRole) {
        return userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN;
    }
}
