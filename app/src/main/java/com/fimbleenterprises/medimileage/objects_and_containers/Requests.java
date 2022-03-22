package com.fimbleenterprises.medimileage.objects_and_containers;

import com.google.gson.Gson;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Requests {

    public static class Request {

        // region CONSTANTS
        public static final String GET = "get";
        public static final String CREATE = "create";
        @NonNls
        public static final String CREATE_MANY = "createmany";
        @NonNls
        public static final String CREATE_NOTE = "createnote";
        @NonNls
        public static final String UPDATE = "update";
        @NonNls
        public static final String UPDATE_MANY = "updatemany";
        @NonNls
        public static final String UPSERT = "upsert";
        @NonNls
        public static final String UPSERT_MANY = "upsertmany";
        @NonNls
        public static final String ASSIGN = "assign";
        @NonNls
        public static final String ASSIGN_MANY = "assignmany";
        @NonNls
        public static final String ASSOCIATE = "associate";
        @NonNls
        public static final String ASSOCIATE_MANY = "associatemany";
        @NonNls
        public static final String DELETE = "delete";
        @NonNls
        public static final String DELETE_MANY = "deletemany";
        @NonNls
        public static final String DISASSOCIATE = "disassociate";
        @NonNls
        public static final String DISASSOCIATE_MANY = "disassociatemany";
        @NonNls
        public static final String SET_STATE = "setstate";
        @NonNls
        public static final String SET_STATE_MANY = "setstatemany";
        @NonNls
        public static final String CAN_AUTHENTICATE = "usercangetproxy";
        @NonNls
        public static final String SEARCHSP = "searchsp";
        @NonNls
        public static final String UPDATE_QUOTE = "updatequote";
        @NonNls
        public static final String CREATE_QUOTE = "createquote";
        @NonNls
        public static final String ADD_QUOTE_PRODUCTS = "addquoteproducts";
        @NonNls
        public static final String ADD_QUOTE_FINANCIAL_SOLUTIONS = "addquotefinsolutions";
        // endregion

        public enum Function {
            GET, CREATE, CREATE_MANY, UPDATE, UPDATE_MANY, UPSERT, UPSERT_MANY, ASSIGN, ASSIGN_MANY, ASSOCIATE, ASSOCIATE_MANY,
            DELETE, DELETE_MANY, DISASSOCIATE, DISASSOCIATE_MANY, SET_STATE, SET_STATE_MANY, CAN_AUTHENTICATE, CREATE_NOTE, SEARCHSP,
            UPDATEQUOTE, CREATEQUOTE, ADD_QUOTE_FINANCIAL_SOLUTIONS, ADD_QUOTE_PRODUCTS
        }

        private String getFunctionName(Enum<Function> function) {
            switch (function.ordinal()) {
                case 1:
                    return CREATE;
                case 2:
                    return CREATE_MANY;
                case 3:
                    return UPDATE;
                case 4:
                    return UPDATE_MANY;
                case 5:
                    return UPSERT;
                case 6:
                    return UPSERT_MANY;
                case 7:
                    return ASSIGN;
                case 8:
                    return ASSIGN_MANY;
                case 9:
                    return ASSOCIATE;
                case 10:
                    return ASSOCIATE_MANY;
                case 11:
                    return DELETE;
                case 12:
                    return DELETE_MANY;
                case 13:
                    return DISASSOCIATE;
                case 14:
                    return DISASSOCIATE_MANY;
                case 15:
                    return SET_STATE;
                case 16:
                    return SET_STATE_MANY;
                case 17:
                    return CAN_AUTHENTICATE;
                case 18:
                    return CREATE_NOTE;
                case 19:
                    return SEARCHSP;
                case 20:
                    return UPDATE_QUOTE;
                case 21:
                    return CREATE_QUOTE;
                case 22:
                    return ADD_QUOTE_FINANCIAL_SOLUTIONS;
                case 23:
                    return ADD_QUOTE_PRODUCTS;
                default:
                    return GET;
            }
        }

        public String function;
        public ArrayList<Argument> arguments = new ArrayList<>();

        public Request() {
        }

        public Request(String function, ArrayList<Argument> arguments) {
            this.function = function;
            this.arguments = arguments;
        }

        public Request(Function function, ArrayList<Argument> arguments) {
            this.function = getFunctionName(function);
            this.arguments = arguments;
        }

        public Request(String function) {
            this.function = function;
        }

        public Request(Function function) {
            this.function = getFunctionName(function);
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    @NonNls
    public static class Argument {
        public String name;
        public Object value;

        public Argument(@NonNls @Nullable String name, Object value) {
            if (name == null) name = "not supplied";
            this.name = name;
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            try {
                return "Argument: " + name + " | Value: + " + value.toString();
            } catch (Exception e) {
                return Objects.requireNonNull(e.getLocalizedMessage());
            }
        }
    }
}
