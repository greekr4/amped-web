0 : {
		bind(1300);
		move(0,1.5);
		changeFace(2);
		fadeinAnimation( 220.0 , 220.0 , 220.0 , 4.5 , 0, 0.2) ;
		ignoreAttack(900);
	}
250 : {
		stop();
		startAnimation( 220.0 , 221.0 , 221.0 , 8.0 , 0) ;
		makeParticle("fcharge.ptc",0.0,0.0,0.4,0,1,0);
	}
400 : {
		makeShapeModel("/effect/explode1.oa",0,0.0,0.8,0,1,0);
		makeShapeModel("/effect/honoo.oa",0,0.1,0.8,0,1,0);
		attack("erupt1");
		startAnimation( 221.0 , 223.0 , 221.0 , 24.0 , 1) ;
	}
600 : {
		makeShapeModel("/effect/explode1.oa",0,0.0,1.6,0,1,0);
		makeShapeModel("/effect/honoo.oa",0,0.1,1.6,0,1,0);
		attack("erupt2");
	}
800 : {
		makeShapeModel("/effect/explode2.oa",0,0.0,2.4,0,1,0);
		makeShapeModel("/effect/crash.oa",0,0.1,2.4,0,1,0);
		attack("erupt3");
	}
1300 : {
		recover();
	}
