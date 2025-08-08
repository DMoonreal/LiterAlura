package com.example.literatura.service;

public interface IDataConvetor {
    <T> T getData(String json, Class<T> clase);
}
