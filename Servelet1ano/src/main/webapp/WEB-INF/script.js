// CARROSSEL 1 - CELULARES

const celulares = [
    "Imagens/CelularInterativo1.svg",
    "Imagens/CelularInterativo2.svg"
]

let indiceAtual = 0;

const imagemCelular = document.getElementById("imagemCelular");
const botaoAnterior = document.getElementById("botaoAnterior");
const botaoProximo = document.getElementById("botaoProximo");

botaoProximo.addEventListener("click", () =>{
    indiceAtual = (indiceAtual + 1) % celulares.length;
    imagemCelular.setAttribute("src", celulares[indiceAtual])
});

botaoAnterior.addEventListener("click", () =>{
    indiceAtual = (indiceAtual - 1 + celulares.length) % celulares.length;
    imagemCelular.setAttribute("src", celulares[indiceAtual])

});


// CARROSEL 3 - GASES POLUENTES
/*
const cardgases =[
  "Imagens/card_gas1.svg",
  "Imagens/card_gas2.svg",
  "Imagens/card_gas3.svg",
  "Imagens/card_gas4.svg",
  "Imagens/card_gas5.svg",
  "Imagens/card_gas6.svg",
  "Imagens/card_gas7.svg"
]

const setaEsquerdaVerde = "Imagens/botao_esquerdo_gases_verde.svg"
const setaEsquerdaRoxo = "Imagens/botao_esquerdo_gases_roxo.svg"
const setaDireitaVerde = "Imagens/botao_direito_gases_verde.svg"
const setaDireitaRoxo = "Imagens/botao_direito_gases_roxo.svg"

let indiceRiscoAtual = 0;

const cardEsquerda = document.getElementById("cardEsquerda");
const cardCentro = document.getElementById("cardCentro");
const cardDireita = document.getElementById("cardDireita");

const botaoAnteriorCard = document.getElementById("botaoAnteriorCard");
const botaoProximoCard = document.getElementById("botaoProximoCard");
const iconeSetaEsquerda = document.getElementById("iconeSetaEsquerda");
const iconeSetaDireita = document.getElementById("iconeSetaDireita");

function atualizarCarroseis() {
  const totalCards = cardgases.length;
  const indiceEsquerda = (indiceRiscoAtual - 1 + totalCards) % totalCards;
  const indiceDireita = (indiceRiscoAtual + 1) % totalCards;

  cardEsquerda.setAttribute("src", cardgases[indiceEsquerda]);
  cardCentro.setAttribute("src", cardgases[indiceRiscoAtual]);
  cardDireita.setAttribute("src", cardgases[indiceDireita]);
}

botaoProximoCard.addEventListener("click", () => {
  indiceRiscoAtual = (indiceRiscoAtual + 1) % cardgases.length;
  atualizarCarroseis();
});

botaoAnteriorCard.addEventListener("click", () => {
  indiceRiscoAtual = (indiceRiscoAtual - 1 + cardgases.length) % cardgases.length;
  atualizarCarroseis();
});*/