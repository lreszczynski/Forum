import React from 'react';

export interface IMainProps {
  longText?: boolean;
}

export default function Main({ longText = true }: IMainProps) {
  if (longText) {
    return (
      <div>
        <p>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque a
          pharetra tortor, vel blandit justo. Aenean condimentum mattis ligula
          quis lacinia. Nam vitae nibh egestas, vulputate arcu sit amet, commodo
          sapien. Pellentesque euismod eleifend turpis et convallis. Praesent ut
          facilisis orci. Curabitur nisl neque, porttitor at interdum sed,
          vulputate at ante. Curabitur hendrerit faucibus posuere. Sed non
          dictum dolor, vel mollis tellus. Praesent dapibus sem nec tellus
          convallis, vel tempor felis tempus. Nullam laoreet nibh nec ex
          tincidunt consequat. Sed a porta nibh. Fusce vel sollicitudin velit.
          Proin aliquet, erat in interdum faucibus, sapien velit maximus ex, eu
          ullamcorper neque leo id felis. Pellentesque accumsan nisl sit amet
          nisl tincidunt hendrerit. Aenean dictum dui tortor, non elementum
          velit maximus nec. Morbi hendrerit viverra leo, vel ullamcorper tellus
          efficitur in. Vivamus molestie, nibh id porta molestie, metus erat
          dictum sapien, vel laoreet risus leo nec est. Aenean elementum mattis
          nunc id consectetur. Donec mollis, nulla condimentum sagittis pretium,
          neque enim auctor erat, at porta justo lorem vel elit. Cras ligula mi,
          congue vel sapien eu, aliquam malesuada velit. Donec facilisis
          suscipit nisl ac pretium. Donec in velit nunc. Duis eu tellus id dui
          elementum rhoncus. Quisque tincidunt lacinia dapibus. Phasellus quis
          mi diam. Nulla semper semper lorem, non maximus justo auctor non.
          Praesent ac leo tristique ipsum hendrerit scelerisque a sit amet
          justo. Maecenas ac nisl euismod diam porta tincidunt. Cras ac nulla
          massa. Maecenas blandit sollicitudin nisl, euismod aliquet elit rutrum
          at. Curabitur accumsan vulputate interdum. In nisi tortor, pretium in
          leo in, elementum facilisis ex. Vivamus vestibulum felis tortor, vitae
          varius eros fermentum et. Morbi sed lectus massa. Sed placerat leo
          nulla, sed imperdiet diam mollis quis. Fusce erat urna, aliquam non
          faucibus et, pellentesque et magna. Suspendisse faucibus condimentum
          eros at ultrices. Nullam faucibus elit ut justo convallis, quis porta
          ipsum accumsan. Pellentesque et magna quis turpis mattis tincidunt sed
          at diam. Morbi eget felis non lorem cursus egestas ut eu metus. In
          rhoncus urna ut orci fringilla congue. In dictum eu orci nec suscipit.
          Class aptent taciti sociosqu ad litora torquent per conubia nostra,
          per inceptos himenaeos. Cras malesuada consectetur sodales. Curabitur
          ut laoreet justo, porttitor lacinia tortor. Mauris et accumsan nunc.
          In accumsan nulla sit amet tellus gravida hendrerit. Integer
          tincidunt, neque sit amet suscipit rutrum, nisi dui sollicitudin
          libero, eget euismod dui lorem sed nulla. Donec varius purus in odio
          rutrum, ut dignissim sapien lacinia. Nam volutpat vehicula dolor
          bibendum semper. Donec pharetra, nisi eget consectetur finibus, felis
          sem molestie eros, vel luctus augue lectus id eros. Sed quis arcu non
          tellus elementum posuere eu nec arcu. Pellentesque suscipit, metus a
          malesuada aliquet, mauris erat volutpat neque, sed tempor nunc mauris
          condimentum nunc. Nam non imperdiet ante. Class aptent taciti sociosqu
          ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam
          pretium fermentum cursus. Aliquam sit amet dui orci. Pellentesque
          auctor eu velit at consectetur. In ultrices mollis augue vitae porta.
        </p>
        <p>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque a
          pharetra tortor, vel blandit justo. Aenean condimentum mattis ligula
          quis lacinia. Nam vitae nibh egestas, vulputate arcu sit amet, commodo
          sapien. Pellentesque euismod eleifend turpis et convallis. Praesent ut
          facilisis orci. Curabitur nisl neque, porttitor at interdum sed,
          vulputate at ante. Curabitur hendrerit faucibus posuere. Sed non
          dictum dolor, vel mollis tellus. Praesent dapibus sem nec tellus
          convallis, vel tempor felis tempus. Nullam laoreet nibh nec ex
          tincidunt consequat. Sed a porta nibh. Fusce vel sollicitudin velit.
          Proin aliquet, erat in interdum faucibus, sapien velit maximus ex, eu
          ullamcorper neque leo id felis. Pellentesque accumsan nisl sit amet
          nisl tincidunt hendrerit. Aenean dictum dui tortor, non elementum
          velit maximus nec. Morbi hendrerit viverra leo, vel ullamcorper tellus
          efficitur in. Vivamus molestie, nibh id porta molestie, metus erat
          dictum sapien, vel laoreet risus leo nec est. Aenean elementum mattis
          nunc id consectetur. Donec mollis, nulla condimentum sagittis pretium,
          neque enim auctor erat, at porta justo lorem vel elit. Cras ligula mi,
          congue vel sapien eu, aliquam malesuada velit. Donec facilisis
          suscipit nisl ac pretium. Donec in velit nunc. Duis eu tellus id dui
          elementum rhoncus. Quisque tincidunt lacinia dapibus. Phasellus quis
          mi diam. Nulla semper semper lorem, non maximus justo auctor non.
          Praesent ac leo tristique ipsum hendrerit scelerisque a sit amet
          justo. Maecenas ac nisl euismod diam porta tincidunt. Cras ac nulla
          massa. Maecenas blandit sollicitudin nisl, euismod aliquet elit rutrum
          at. Curabitur accumsan vulputate interdum. In nisi tortor, pretium in
          leo in, elementum facilisis ex. Vivamus vestibulum felis tortor, vitae
          varius eros fermentum et. Morbi sed lectus massa. Sed placerat leo
          nulla, sed imperdiet diam mollis quis. Fusce erat urna, aliquam non
          faucibus et, pellentesque et magna. Suspendisse faucibus condimentum
          eros at ultrices. Nullam faucibus elit ut justo convallis, quis porta
          ipsum accumsan. Pellentesque et magna quis turpis mattis tincidunt sed
          at diam. Morbi eget felis non lorem cursus egestas ut eu metus. In
          rhoncus urna ut orci fringilla congue. In dictum eu orci nec suscipit.
          Class aptent taciti sociosqu ad litora torquent per conubia nostra,
          per inceptos himenaeos. Cras malesuada consectetur sodales. Curabitur
          ut laoreet justo, porttitor lacinia tortor. Mauris et accumsan nunc.
          In accumsan nulla sit amet tellus gravida hendrerit. Integer
          tincidunt, neque sit amet suscipit rutrum, nisi dui sollicitudin
          libero, eget euismod dui lorem sed nulla. Donec varius purus in odio
          rutrum, ut dignissim sapien lacinia. Nam volutpat vehicula dolor
          bibendum semper. Donec pharetra, nisi eget consectetur finibus, felis
          sem molestie eros, vel luctus augue lectus id eros. Sed quis arcu non
          tellus elementum posuere eu nec arcu. Pellentesque suscipit, metus a
          malesuada aliquet, mauris erat volutpat neque, sed tempor nunc mauris
          condimentum nunc. Nam non imperdiet ante. Class aptent taciti sociosqu
          ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam
          pretium fermentum cursus. Aliquam sit amet dui orci. Pellentesque
          auctor eu velit at consectetur. In ultrices mollis augue vitae porta.
        </p>
        <p>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque a
          pharetra tortor, vel blandit justo. Aenean condimentum mattis ligula
          quis lacinia. Nam vitae nibh egestas, vulputate arcu sit amet, commodo
          sapien. Pellentesque euismod eleifend turpis et convallis. Praesent ut
          facilisis orci. Curabitur nisl neque, porttitor at interdum sed,
          vulputate at ante. Curabitur hendrerit faucibus posuere. Sed non
          dictum dolor, vel mollis tellus. Praesent dapibus sem nec tellus
          convallis, vel tempor felis tempus. Nullam laoreet nibh nec ex
          tincidunt consequat. Sed a porta nibh. Fusce vel sollicitudin velit.
          Proin aliquet, erat in interdum faucibus, sapien velit maximus ex, eu
          ullamcorper neque leo id felis. Pellentesque accumsan nisl sit amet
          nisl tincidunt hendrerit. Aenean dictum dui tortor, non elementum
          velit maximus nec. Morbi hendrerit viverra leo, vel ullamcorper tellus
          efficitur in. Vivamus molestie, nibh id porta molestie, metus erat
          dictum sapien, vel laoreet risus leo nec est. Aenean elementum mattis
          nunc id consectetur. Donec mollis, nulla condimentum sagittis pretium,
          neque enim auctor erat, at porta justo lorem vel elit. Cras ligula mi,
          congue vel sapien eu, aliquam malesuada velit. Donec facilisis
          suscipit nisl ac pretium. Donec in velit nunc. Duis eu tellus id dui
          elementum rhoncus. Quisque tincidunt lacinia dapibus. Phasellus quis
          mi diam. Nulla semper semper lorem, non maximus justo auctor non.
          Praesent ac leo tristique ipsum hendrerit scelerisque a sit amet
          justo. Maecenas ac nisl euismod diam porta tincidunt. Cras ac nulla
          massa. Maecenas blandit sollicitudin nisl, euismod aliquet elit rutrum
          at. Curabitur accumsan vulputate interdum. In nisi tortor, pretium in
          leo in, elementum facilisis ex. Vivamus vestibulum felis tortor, vitae
          varius eros fermentum et. Morbi sed lectus massa. Sed placerat leo
          nulla, sed imperdiet diam mollis quis. Fusce erat urna, aliquam non
          faucibus et, pellentesque et magna. Suspendisse faucibus condimentum
          eros at ultrices. Nullam faucibus elit ut justo convallis, quis porta
          ipsum accumsan. Pellentesque et magna quis turpis mattis tincidunt sed
          at diam. Morbi eget felis non lorem cursus egestas ut eu metus. In
          rhoncus urna ut orci fringilla congue. In dictum eu orci nec suscipit.
          Class aptent taciti sociosqu ad litora torquent per conubia nostra,
          per inceptos himenaeos. Cras malesuada consectetur sodales. Curabitur
          ut laoreet justo, porttitor lacinia tortor. Mauris et accumsan nunc.
          In accumsan nulla sit amet tellus gravida hendrerit. Integer
          tincidunt, neque sit amet suscipit rutrum, nisi dui sollicitudin
          libero, eget euismod dui lorem sed nulla. Donec varius purus in odio
          rutrum, ut dignissim sapien lacinia. Nam volutpat vehicula dolor
          bibendum semper. Donec pharetra, nisi eget consectetur finibus, felis
          sem molestie eros, vel luctus augue lectus id eros. Sed quis arcu non
          tellus elementum posuere eu nec arcu. Pellentesque suscipit, metus a
          malesuada aliquet, mauris erat volutpat neque, sed tempor nunc mauris
          condimentum nunc. Nam non imperdiet ante. Class aptent taciti sociosqu
          ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam
          pretium fermentum cursus. Aliquam sit amet dui orci. Pellentesque
          auctor eu velit at consectetur. In ultrices mollis augue vitae porta.
        </p>
      </div>
    );
  }
  return (
    <p>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque a
      pharetra tortor, vel blandit justo. Aenean condimentum mattis ligula quis
      lacinia. Nam vitae nibh egestas, vulputate arcu sit amet, commodo sapien.
      Pellentesque euismod eleifend turpis et convallis. Praesent ut facilisis
      orci. Curabitur nisl neque, porttitor at interdum sed, vulputate at ante.
      Curabitur hendrerit faucibus posuere. Sed non dictum dolor, vel mollis
      tellus. Praesent dapibus sem nec tellus convallis, vel tempor felis
      tempus. Nullam laoreet nibh nec ex tincidunt consequat. Sed a porta nibh.
      Fusce vel sollicitudin velit. Proin aliquet, erat in interdum faucibus,
      sapien velit maximus ex, eu ullamcorper neque leo id felis. Pellentesque
      accumsan nisl sit amet nisl tincidunt hendrerit. Aenean dictum dui tortor,
      non elementum velit maximus nec.
    </p>
  );
}
