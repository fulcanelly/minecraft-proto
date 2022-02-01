require 'mkmf'

$CXXFLAGS += "-std=c++20 -O3"

create_header
create_makefile 'varnum'
