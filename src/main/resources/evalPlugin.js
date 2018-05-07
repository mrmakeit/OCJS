function babelEval(babel) {
  var t = babel.types;
  return {
    visitor: {
      CallExpression: function(path) {
				if(t.isIdentifier(path.node.callee,{name:"eval"})){
					path.node.callee = t.identifier("babelEval");
				}
      },
      NewExpression: function(path) {
				if(t.isIdentifier(path.node.callee,{name:"Function"})){
					path.node.callee = t.identifier("BabelFunction");
				}
			}
    }
  };
}

Babel.registerPlugin('babel-eval',babelEval)
