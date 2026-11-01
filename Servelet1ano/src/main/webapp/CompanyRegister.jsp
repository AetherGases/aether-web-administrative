<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Cadastro empresarial | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/CompanyRegister.css?v=5">
</head>
<body>

<div class="tela">
    <div class="blur blur-1"></div>
    <div class="blur blur-2"></div>
    <div class="blur blur-3"></div>
    <div class="blur blur-4"></div>

    <a class="voltar" href="${ctx}/Index.jsp">
        <span>
            <svg viewBox="0 0 22 22" aria-hidden="true"><path d="M14 4l-7 7 7 7" fill="none" stroke="#FFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
        Voltar
    </a>

    <section class="plano-card plano-pulse">
        <h2>aether <span class="verde">Pulse</span></h2>
        <p class="preco"><strong>R$3.300</strong>/por mês</p>
        <hr>
        <ul>
            <li>Quantidade de cadastro limitada a 10 funcionários</li>
            <li>Quantidade de analistas e administradores limitada a 2</li>
            <li>Acompanhamento mensal</li>
        </ul>
    </section>

    <section class="plano-card plano-horizon">
        <h2>aether <span class="roxo">Horizon</span></h2>
        <p class="preco"><strong>R$9.400</strong>/a cada 3 meses</p>
        <hr>
        <ul>
            <li>Tudo do plano aether Pulse</li>
            <li>Quantidade de cadastro de funcionários ilimitada</li>
            <li>Acompanhamento Trimestral</li>
        </ul>
    </section>

    <c:if test="${not empty requestScope.erro}">
        <p class="mensagem mensagem-erro" role="alert" style="top: 16px"><c:out value="${requestScope.erro}"/></p>
    </c:if>
    <c:if test="${not empty requestScope.sucesso}">
        <div class="mensagem mensagem-sucesso" role="status" style="top: 16px">
            <p><c:out value="${requestScope.sucesso}"/></p>
            <c:if test="${not empty requestScope.companyId}">
                <p>Código da empresa: <c:out value="${requestScope.companyId}"/></p>
            </c:if>
        </div>
    </c:if>

    <main class="cartao-form" style="top: 72px; height: 780px">
        <img class="mascote" style="left: 192px; top: 26px" src="${ctx}/Imagens/AekoLogin.svg" alt="">
        <h1 class="pilula-titulo" style="left: 80.5px; top: 137px; width: 385px">Cadastro empresarial</h1>

        <form id="formularioEmpresarial" action="${ctx}/empresa" method="post" accept-charset="UTF-8">
            <label for="empresa" class="visually-hidden">Nome da empresa</label>
            <input class="entrada" style="top: 196px" type="text" id="empresa" name="empresas"
                   placeholder="Nome da empresa" autocomplete="organization"
                   value="<c:out value='${requestScope.empresas}'/>" required>

            <label for="razao" class="visually-hidden">Razão social</label>
            <input class="entrada" style="top: 268px" type="text" id="razao" name="razoes"
                   placeholder="Razão social"
                   value="<c:out value='${requestScope.razoes}'/>" required>

            <%-- Tamanho --%>
            <div class="dropdownPersonalizado" style="top: 340px">
                <span class="dropdownRotulo" id="rotuloTamanho">Tamanho da empresa</span>
                <div class="dropdownCaixa">
                    <button class="dropdownBotao" type="button" aria-haspopup="listbox" aria-expanded="false"
                            aria-labelledby="rotuloTamanho textoTamanho">
                        <span class="dropdownTexto vazio" id="textoTamanho" data-placeholder="Selecionar">Selecionar</span>
                        <span class="dropdownSeta" aria-hidden="true"></span>
                    </button>
                    <ul class="dropdownLista" role="listbox" aria-labelledby="rotuloTamanho">
                        <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="grande" ${requestScope.tamanhos eq 'grande' ? 'data-selecionado' : ''}>Grande empresa</li>
                        <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="media" ${requestScope.tamanhos eq 'media' ? 'data-selecionado' : ''}>Média empresa</li>
                        <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="pequena" ${requestScope.tamanhos eq 'pequena' ? 'data-selecionado' : ''}>Pequena empresa</li>
                    </ul>
                    <input type="hidden" id="tamanho" name="tamanhos" value="">
                </div>
            </div>

            <label for="cnpj" class="visually-hidden">CNPJ</label>
            <input class="entrada" style="top: 422px" type="text" id="cnpj" name="campoCNPJ"
                   placeholder="CNPJ" inputmode="numeric" autocomplete="off"
                   value="<c:out value='${requestScope.campoCNPJ}'/>" required>

            <label for="email" class="visually-hidden">E-mail</label>
            <input class="entrada" style="top: 494px" type="email" id="email" name="campoEmail"
                   placeholder="E-mail" autocomplete="email"
                   value="<c:out value='${requestScope.campoEmail}'/>" required>

            <%-- Plano --%>
            <div class="dropdownPersonalizado" style="top: 556px">
                <span class="dropdownRotulo" id="rotuloPlano">Plano</span>
                <div class="dropdownCaixa">
                    <button class="dropdownBotao" type="button" aria-haspopup="listbox" aria-expanded="false"
                            aria-labelledby="rotuloPlano textoPlano">
                        <span class="dropdownTexto vazio" id="textoPlano" data-placeholder="Selecionar">Selecionar</span>
                        <span class="dropdownSeta" aria-hidden="true"></span>
                    </button>
                    <ul class="dropdownLista" role="listbox" aria-labelledby="rotuloPlano">
                        <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="Aether Pulse" ${requestScope.planos eq 'Aether Pulse' ? 'data-selecionado' : ''}>Aether Pulse</li>
                        <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="Aether Horizon" ${requestScope.planos eq 'Aether Horizon' ? 'data-selecionado' : ''}>Aether Horizon</li>
                    </ul>
                    <input type="hidden" id="plano" name="planos" value="">
                </div>
            </div>

            <button class="botao-enviar" style="top: 638px" type="submit">Cadastrar</button>

            <p class="link-conta" style="top: 711px">
                Já possui uma conta? <a href="${ctx}/login">clique aqui</a>
            </p>
        </form>
    </main>
</div>

<script>
    (function () {
        var form = document.getElementById('formularioEmpresarial');
        var dropdowns = [].slice.call(document.querySelectorAll('.dropdownPersonalizado'));

        function fechar(dd) {
            dd.classList.remove('dropdownAberto');
            dd.querySelector('.dropdownBotao').setAttribute('aria-expanded', 'false');
        }

        function fecharTodos() { dropdowns.forEach(fechar); }

        dropdowns.forEach(function (dd) {
            var botao  = dd.querySelector('.dropdownBotao');
            var texto  = dd.querySelector('.dropdownTexto');
            var opcoes = [].slice.call(dd.querySelectorAll('.dropdownOpcao'));
            var campo  = dd.querySelector('input[type="hidden"]');

            function selecionar(op) {
                opcoes.forEach(function (o) {
                    o.removeAttribute('data-selecionado');
                    o.setAttribute('aria-selected', 'false');
                });
                op.setAttribute('data-selecionado', '');
                op.setAttribute('aria-selected', 'true');
                texto.textContent = op.textContent;
                texto.classList.remove('vazio');
                campo.value = op.getAttribute('data-valor');
                dd.classList.remove('invalido');
            }

            function abrir() {
                fecharTodos();
                dd.classList.add('dropdownAberto');
                botao.setAttribute('aria-expanded', 'true');
                var atual = opcoes.filter(function (o) { return o.hasAttribute('data-selecionado'); })[0];
                (atual || opcoes[0]).focus();
            }

            function escolher(op) {
                selecionar(op);
                fechar(dd);
                botao.focus();
            }

            var inicial = opcoes.filter(function (o) { return o.hasAttribute('data-selecionado'); })[0];
            if (inicial) { selecionar(inicial); }

            botao.addEventListener('click', function () {
                dd.classList.contains('dropdownAberto') ? fechar(dd) : abrir();
            });

            botao.addEventListener('keydown', function (e) {
                if (e.key === 'ArrowDown' || e.key === 'ArrowUp') { e.preventDefault(); abrir(); }
            });

            opcoes.forEach(function (op, i) {
                op.addEventListener('click', function () { escolher(op); });
                op.addEventListener('keydown', function (e) {
                    if (e.key === 'ArrowDown') { e.preventDefault(); (opcoes[i + 1] || opcoes[0]).focus(); }
                    else if (e.key === 'ArrowUp') { e.preventDefault(); (opcoes[i - 1] || opcoes[opcoes.length - 1]).focus(); }
                    else if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); escolher(op); }
                    else if (e.key === 'Escape') { e.preventDefault(); fechar(dd); botao.focus(); }
                    else if (e.key === 'Tab') { fechar(dd); }
                });
            });
        });

        document.addEventListener('click', function (e) {
            dropdowns.forEach(function (dd) { if (!dd.contains(e.target)) { fechar(dd); } });
        });

        form.addEventListener('submit', function (e) {
            var primeiro = null;
            dropdowns.forEach(function (dd) {
                if (!dd.querySelector('input[type="hidden"]').value) {
                    dd.classList.add('invalido');
                    if (!primeiro) { primeiro = dd.querySelector('.dropdownBotao'); }
                }
            });
            if (primeiro) { e.preventDefault(); primeiro.focus(); }
        });
    })();
</script>

</body>
</html>