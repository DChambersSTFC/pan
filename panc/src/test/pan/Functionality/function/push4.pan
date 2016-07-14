#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template push4;

'/y' = {
  y[0] = 4;
  y[1] = 5;
  y[2] = 6;
};

'/x' = {
  y = value('/y');
  x[0] = 1;
  x[1] = 2;
  x[2] = 3;
  x = push(x[0],x[1],x[2],y[0],y[1],y[2]);
};

'/result' = {
  x = value('/x');
  (length(x) == 6 && x[2] == 3 && x[5] == 6);
};
