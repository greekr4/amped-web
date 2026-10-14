0 : {
		bind(1200);
		stop();
		changeFace(2);
		fadeinAnimation( 290.0 , 290.0 , 290.0 , 3.0 , 0, 0.2) ;
		ignoreAttack(1300);
		jump(3.5);
		makeParticle("fcharge.ptc",0.0,0.3,0.0,0,1,0);
	}
300 : {
		startAnimation( 291.0 , 293.0 , 293.0 , 10 , 0) ;
		makeShapeModel("/effect/explode1.oa",0,0.3,0.3,0,1,0);
	}
400 : {
		attack("meteor");
		makeShapeModel("/effect/honoo.oa",0,0.2,0.4,0,0,1,1);
	}
520 : {
		startAnimation( 291.0 , 293.0 , 293.0 , 10 , 0) ;
		attack("meteorL");
		attack("meteorR");
	}
640 : {
		startAnimation( 291.0 , 293.0 , 293.0 , 10 , 0) ;
		attack("meteorLL");
		attack("meteorRR");
		makeShapeModel("/effect/explode2.oa",0,0.3,0.3,0,1,0);
	}
800 : {
		startAnimation( 293.0 , 294.0 , 294.0 , 3.5 , 0) ;
	}
1200 : {
		recover();
	}
