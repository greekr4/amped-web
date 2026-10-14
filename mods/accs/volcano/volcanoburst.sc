0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 250.0 , 250.0 , 250.0 , 4.5 , 0, 0.16) ;
		ignoreAttack(3000);
		bind(2800);
		makeParticle("fcharge.ptc",0.0,0.0,0.0,0,1,0);
		makeShapeModel("/effect/fire1.oa",0,0.3,0,0,1,0);
	}
400 : {
		landScript("volcanoland.sc");
		move(0,1.6);
		jump(11.0);
		changeFace(1);
		makeShapeModel("/effect/jet.oa",0,-0.2,0,0,-1,0,1);
		makeShapeModel("/effect/honoo.oa",0,0.2,0,0,1,0,1);
		startAnimation( 290.0 , 291.0 , 291.0 , 8.0 , 0) ;
	}
1100 : {
		move(0,-11,0.4);
		makeShapeModel("/effect/jet.oa",0,0.3,0,0,1,0,1);
		makeShapeModel("/effect/fire1.oa",0,-0.2,0,0,-1,0,1);
		startAnimation( 335.0 , 336.0 , 336.0 , 10.0 , 0) ;
	}
3000 : {
		recover();
	}
