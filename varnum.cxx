#include "ruby.h"

#include <thread>
#include <iostream>
#include <ranges>

using namespace std;



VALUE read_var_len(VALUE self) {    
    long value = 0;
    int length = 0;
    char currentByte;

    static auto func = rb_intern("read");

    while (true) {
        currentByte = FIX2INT(rb_funcall(self, func, 0));
        value |= (currentByte & 0x7F) << (length * 7);
        
        length += 1;
        if (length > 10) {
            rb_raise(rb_eException, "varnum too long");
        }

        if ((currentByte & 0x80) != 0x80) {
            break;
        }
    }
    return INT2FIX(value);
}

VALUE write_var_len(VALUE self, VALUE num) {
    int val = FIX2INT(num);

    static auto func = rb_intern("write");
    while (true) {
        if ((val & ~0x7F) == 0) {
            rb_funcall(self, func, 1, INT2FIX(val));
           return Qnil;
        }

        rb_funcall(self, func, 1, INT2FIX((val & 0x7F) | 0x80));

        
        val >>= 7;
    }
    
    return Qnil;
}



extern "C" void Init_varnum() {
    VALUE mod = rb_define_module("VarNum");
    rb_define_method(mod, "read_var_num", read_var_len, 0);
    rb_define_method(mod, "write_var_num", write_var_len, 1);

}