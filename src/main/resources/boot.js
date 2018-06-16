loader = (function(es6loader,evalFunc){
  return {
    es6Eval:function(code){
      code = es6loader.eval(code)
      evalFunc(code);
    },
    es5Eval:function(code){
      evalFunc(code);
    }
  }
})(loader,eval);
