// CARROSSEL 1 - CELULARES

const celulares = [
    "WEB-INF/Imagens/CelularInterativo1.svg",
    "WEB-INF/Imagens/CelularInterativo2.svg"
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

