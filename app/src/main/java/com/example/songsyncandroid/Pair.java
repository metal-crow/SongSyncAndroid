package com.example.songsyncandroid;

public class Pair<T1, T2> {
    
    T1 var1;
    T2 var2;
    
    public Pair(T1 var1, T2 var2) {
        this.var1=var1;
        this.var2=var2;
    }

    public T2 getValue1() {
        return var2;
    }

    public T1 getValue0() {
        return var1;
    }
}
